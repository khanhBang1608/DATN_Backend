package com.java.fashionshop.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.java.fashionshop.dto.*;
import com.java.fashionshop.entity.*;
import com.java.fashionshop.jpa.*;
import com.java.fashionshop.request.OrderCreateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private JpaOrder orderRepository;

    @Autowired
    private JpaUser userRepository;

    @Autowired
    private JpaDiscount discountRepository;

    @Autowired
    private JpaProductVariant productVariantRepository;

    @Autowired
    private JpaOrderDetail jpaOrderDetail;

	@Autowired
	private JpaOrderReturnEntity jpaOrderReturnEntity;

	public OrderEntity save(OrderEntity order) {
		return orderRepository.save(order);
	}

    private Integer getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
        return user.getUserId();
    }

	@Transactional
	public OrderDTO createOrder(OrderCreateRequest request) {
		Integer userId = getAuthenticatedUserId();
		if (request.getIdempotencyKey() != null) {
			OrderEntity existingOrder = orderRepository
					.findByIdempotencyKey((request.getIdempotencyKey()));
			if (existingOrder != null) {
				return convertToDTO(existingOrder);
			}
		}
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user với id: " + userId));

		OrderEntity order = new OrderEntity();
		order.setUser(user);
		order.setOrderDate(LocalDateTime.now());
		order.setAddress(request.getAddress());
		order.setIdempotencyKey(request.getIdempotencyKey());
		order.setPaymentMethod(request.getPaymentMethod());
		order.setStatus(0); // 0: Pending
		if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
			order.setPaymentStatus(1); // 1: Paid
		} else {
			order.setPaymentStatus(0); // 0: Unpaid
		}
		order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
		order.setDiscountAmount(BigDecimal.ZERO);

		if (request.getDiscountCode() != null) {
			DiscountEntity discount = discountRepository.findByDiscountCode(request.getDiscountCode()).orElse(null);
			if (discount != null && discount.getStatus() && discount.getEndDate().isAfter(LocalDate.now())) {
				if (discount.getQuantityLimit() != null && discount.getQuantityLimit() > 0) {
					discount.setQuantityLimit(discount.getQuantityLimit() - 1);
					discountRepository.save(discount);
					order.setDiscountCode(discount.getDiscountCode());

					// ✅ Dùng discountAmount từ frontend
					BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount()
							: BigDecimal.ZERO;
					order.setDiscountAmount(discountAmount);
				} else {
					throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
				}
			}
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (OrderCreateRequest.OrderDetailRequest detailRequest : request.getOrderDetails()) {
			if (detailRequest.getQuantity() <= 0) {
				throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
			}

			ProductVariantEntity variant = productVariantRepository.findById(detailRequest.getProductVariantId())
					.orElseThrow(() -> new EntityNotFoundException(
							"Không tìm thấy product variant với id: " + detailRequest.getProductVariantId()));

			if (variant.getStock() < detailRequest.getQuantity()) {
				throw new RuntimeException("Hết hàng cho variant: " + variant.getProductVariantId());
			}

			BigDecimal price = detailRequest.getPrice();
			if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
				price = variant.getPrice(); // fallback nếu thiếu
			}

            OrderDetailEntity detail = new OrderDetailEntity();
            detail.setOrder(order);
            detail.setQuantity(detailRequest.getQuantity());
            detail.setPrice(price); // ✅ dùng giá từ request nếu có
            detail.setProductVariant(variant);

			order.getOrderDetails().add(detail);
			totalAmount = totalAmount.add(price.multiply(new BigDecimal(detail.getQuantity())));
		}
		adjustStockForOrder(order, false);
		order.setTotalAmount(totalAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
		order = orderRepository.save(order);

        return convertToDTO(order);
    }

    public List<OrderDTO> getUserOrders() {
        Integer userId = getAuthenticatedUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user với id: " + userId));

		return orderRepository.findByUser(user).stream().map(this::convertToDTO).collect(Collectors.toList());
	}

    public OrderDTO getOrderDetails(Integer orderId) {
        Integer userId = getAuthenticatedUserId();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền truy cập order");
        }

        return convertToDTO(order);
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        Integer userId = getAuthenticatedUserId();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền hủy order");
        }

        if (order.getStatus() != 0) {
            throw new IllegalStateException("Chỉ có thể hủy order ở trạng thái Pending");
        }

        // Hoàn stock nếu order đã trừ stock (trường hợp bất thường)
        if (order.getStatus() == 3) {
            adjustStockForOrder(order, true);
        }

        order.setStatus(5); // 5: Cancelled
        orderRepository.save(order);
    }

	@Transactional
	public OrderDTO updateOrder(Integer orderId, OrderDTO orderDTO) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		// Lưu trạng thái trước để so sánh
		int previousStatus = order.getStatus();

		order.setAddress(orderDTO.getAddress());
		order.setPaymentMethod(orderDTO.getPaymentMethod());
		order.setStatus(orderDTO.getStatus());
		order.setPaymentStatus(orderDTO.getPaymentStatus());
		order.setShippingFee(orderDTO.getShippingFee());
		order.setDiscountAmount(orderDTO.getDiscountAmount());

		if (orderDTO.getUserId() != null) {
			UserEntity user = userRepository.findById(orderDTO.getUserId()).orElseThrow(
					() -> new EntityNotFoundException("Không tìm thấy user với id: " + orderDTO.getUserId()));
			order.setUser(user);
		}

		if (orderDTO.getDiscountCode() != null) {
			DiscountEntity discount = discountRepository.findByDiscountCode(orderDTO.getDiscountCode()).orElseThrow(
					() -> new EntityNotFoundException("Không tìm thấy discount với id: " + orderDTO.getDiscountCode()));
			order.setDiscountCode(discount.getDiscountCode());
			order.setDiscountAmount(calculateDiscount(order, discount));
		} else {
			order.setDiscountCode(null);
			order.setDiscountAmount(BigDecimal.ZERO);
		}

		// Xử lý OrderDetails
		order.getOrderDetails().clear();
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (OrderDetailDTO detailDTO : orderDTO.getOrderDetails()) {
			if (detailDTO.getQuantity() <= 0) {
				throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
			}

			ProductVariantEntity variant = productVariantRepository.findById(detailDTO.getProductVariantId())
					.orElseThrow(() -> new EntityNotFoundException(
							"Không tìm thấy product variant với id: " + detailDTO.getProductVariantId()));

			if (variant.getStock() < detailDTO.getQuantity()) {
				throw new RuntimeException("Hết hàng cho variant: " + variant.getProductVariantId());
			}

			OrderDetailEntity detail = new OrderDetailEntity();
			detail.setOrder(order);
			detail.setQuantity(detailDTO.getQuantity());
			detail.setPrice(variant.getPrice());
			detail.setProductVariant(variant);
			order.getOrderDetails().add(detail);
			totalAmount = totalAmount.add(detail.getPrice().multiply(new BigDecimal(detail.getQuantity())));
		}
		if ((orderDTO.getStatus() == 2 || orderDTO.getStatus() == 3) && previousStatus != orderDTO.getStatus()) {
			order.setPaymentStatus(1); // Đã thanh toán
		}

		// Hoàn stock nếu chuyển từ trạng thái 3 sang trạng thái khác
		else if (previousStatus == 4 && orderDTO.getStatus() == 6) {
			adjustStockForOrder(order, true);
			order.setPaymentStatus(2);
		}

		order.setTotalAmount(totalAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
		order = orderRepository.save(order);
		return convertToDTO(order);
	}
    @Transactional
    public void deleteOrder(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		// Hoàn stock nếu order ở trạng thái 3
        if (order.getStatus() == 3) {
            adjustStockForOrder(order, true);
        }

        orderRepository.delete(order);
    }
	private void adjustStockForOrder(OrderEntity order, boolean isRestock) {
		for (OrderDetailEntity detail : order.getOrderDetails()) {
			ProductVariantEntity variant = detail.getProductVariant();
			int quantity = detail.getQuantity();
			if (isRestock) {
				variant.setStock(variant.getStock() + quantity);
			} else {
				variant.setStock(variant.getStock() - quantity);
			}
			productVariantRepository.save(variant);
		}
	}

    private BigDecimal calculateDiscount(OrderEntity order, DiscountEntity discount) {
        BigDecimal total = order.getOrderDetails().stream()
                .map(detail -> detail.getPrice().multiply(new BigDecimal(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = total.multiply(new BigDecimal(discount.getDiscountPercent() / 100.0));
        if (discount.getMaxDiscountAmount() != null) {
            discountAmount = discountAmount.min(new BigDecimal(discount.getMaxDiscountAmount()));
        }
        return discountAmount;
    }

    private OrderDTO convertToDTO(OrderEntity order) {
        List<OrderDetailDTO> orderDetails = order.getOrderDetails().stream()
                .map(detail -> new OrderDetailDTO(
                        detail.getOrderDetailId(),
                        detail.getQuantity(),
                        detail.getPrice(),
                        detail.getProductVariant().getProductVariantId(),
                        detail.getProductVariant().getProduct().getName(),
                        detail.getProductVariant().getImageName(),
                        detail.getProductVariant().getSize().getSizeName(),
                        detail.getProductVariant().getColor().getColorName()
                ))
                .collect(Collectors.toList());

        return new OrderDTO(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getOrderDate(),
                order.getAddress(),
                order.getStatus(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getUser().getUserId(),
                order.getDiscountCode(),
                orderDetails,
                order.getUser().getFullName()
        );
    }

	public Page<OrderDTO> getAllOrders(Pageable pageable) {
		// Ghi đè sort theo status ASC
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by(Sort.Direction.ASC, "status"));

		return orderRepository.findAll(sortedPageable).map(this::convertToDTO);
	}


	public OrderDTO getOrderById(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));
        return convertToDTO(order);
    }


	public byte[] exportInvoicePdf(Integer orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			PdfWriter writer = new PdfWriter(baos);
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf, PageSize.A4);
			document.setMargins(30, 30, 30, 30);

			PdfFont font = PdfFontFactory.createFont("src/main/resources/static/fonts/DejaVuSans.ttf", PdfEncodings.IDENTITY_H);
			PdfFont boldFont = PdfFontFactory.createFont("src/main/resources/static/fonts/DejaVuSans-Bold.ttf", PdfEncodings.IDENTITY_H);
			DeviceRgb brandColor = new DeviceRgb(0, 102, 204);

			Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG")
					.setFont(boldFont)
					.setFontSize(20)
					.setTextAlignment(TextAlignment.CENTER)
					.setFontColor(brandColor)
					.setMarginBottom(10);
			document.add(title);

			document.add(new Paragraph("CÔNG TY CỔ PHẦN MAISON RETAIL MANAGEMENT INTERNATIONAL")
					.setFont(boldFont)
					.setFontSize(12)
					.setTextAlignment(TextAlignment.LEFT));
			document.add(new Paragraph("Địa chỉ: Toà nhà FPT Polytechnic, Đ. Số 22, Thường Thạnh, Cái Răng, Cần Thơ")
					.setFont(font)
					.setFontSize(10));
			document.add(new Paragraph("Số điện thoại:  0378 447 716 | Email: customers@lhex.vn")
					.setFont(font)
					.setFontSize(10)
					.setMarginBottom(20));

			document.add(new Paragraph("")
					.setBorderBottom(new SolidBorder(brandColor, 1))
					.setMarginBottom(10));

			Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
			infoTable.setWidth(UnitValue.createPercentValue(100));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Mã hóa đơn: #" + order.getOrderId()).setFont(boldFont).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Ngày xuất: " + LocalDateTime.now().format(dateTimeFormatter))
							.setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Khách hàng: " + order.getUser().getFullName()).setFont(boldFont).setFontSize(10)));
			if (order.getOrderDate() instanceof LocalDateTime) {
				infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
						.add(new Paragraph("Ngày đặt hàng: " + ((LocalDateTime) order.getOrderDate()).format(dateTimeFormatter))
								.setFont(font).setFontSize(10)));
			} else {
				infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
						.add(new Paragraph("Ngày đặt hàng: " + order.getOrderDate().toString())
								.setFont(font).setFontSize(10)));
			}
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Địa chỉ: " + order.getAddress()).setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Phương thức thanh toán: " + order.getPaymentMethod()).setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Trạng thái thanh toán: " + (order.getPaymentStatus() == 1 ? "Đã thanh toán" : "Chưa thanh toán"))
							.setFont(font).setFontSize(10)));
			document.add(infoTable);
			document.add(new Paragraph("").setMarginBottom(20));

			// Bảng chi tiết đơn hàng
			float[] columnWidths = {1, 4, 1, 2, 2};
			Table table = new Table(UnitValue.createPercentArray(columnWidths));
			table.setWidth(UnitValue.createPercentValue(100));
			table.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));

			// Tiêu đề bảng
			table.addHeaderCell(new Cell().setBackgroundColor(brandColor)
					.setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE)
					.setTextAlignment(TextAlignment.CENTER)
					.add(new Paragraph("STT")));
			table.addHeaderCell(new Cell().setBackgroundColor(brandColor)
					.setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE)
					.setTextAlignment(TextAlignment.CENTER)
					.add(new Paragraph("Sản phẩm")));
			table.addHeaderCell(new Cell().setBackgroundColor(brandColor)
					.setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE)
					.setTextAlignment(TextAlignment.CENTER)
					.add(new Paragraph("Số lượng")));
			table.addHeaderCell(new Cell().setBackgroundColor(brandColor)
					.setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE)
					.setTextAlignment(TextAlignment.CENTER)
					.add(new Paragraph("Đơn giá")));
			table.addHeaderCell(new Cell().setBackgroundColor(brandColor)
					.setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE)
					.setTextAlignment(TextAlignment.CENTER)
					.add(new Paragraph("Thành tiền")));

			// Nội dung bảng
			int index = 1;
			BigDecimal subtotal = BigDecimal.ZERO;
			for (OrderDetailEntity detail : order.getOrderDetails()) {
				BigDecimal total = detail.getPrice().multiply(new BigDecimal(detail.getQuantity()));
				subtotal = subtotal.add(total);

				table.addCell(new Cell().setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER)
						.add(new Paragraph(String.valueOf(index++))));
				table.addCell(new Cell().setFont(font).setFontSize(10)
						.add(new Paragraph(detail.getProductVariant().getProduct().getName() +
								" (" + detail.getProductVariant().getSize().getSizeName() + ", " +
								detail.getProductVariant().getColor().getColorName() + ")")));
				table.addCell(new Cell().setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER)
						.add(new Paragraph(String.valueOf(detail.getQuantity()))));
				table.addCell(new Cell().setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)
						.add(new Paragraph(String.format("%,.0f VND", detail.getPrice()))));
				table.addCell(new Cell().setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)
						.add(new Paragraph(String.format("%,.0f VND", total))));
			}
			document.add(table);
			document.add(new Paragraph("").setMarginBottom(20));

			Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
			summaryTable.setWidth(UnitValue.createPercentValue(50));
			summaryTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Tạm tính").setFont(font).setFontSize(10)));
			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph(String.format("%,.0f VND", subtotal)).setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)));

			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Phí vận chuyển").setFont(font).setFontSize(10)));
			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph(String.format("%,.0f VND", order.getShippingFee())).setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)));

			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Giảm giá").setFont(font).setFontSize(10)));
			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph(String.format("-%,.0f VND", order.getDiscountAmount())).setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)));

			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Tổng cộng").setFont(boldFont).setFontSize(12)));
			summaryTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph(String.format("%,.0f VND", order.getTotalAmount())).setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.RIGHT)));

			document.add(summaryTable);
			document.add(new Paragraph("").setMarginBottom(20));

			Paragraph footer = new Paragraph("Cảm ơn quý khách đã mua sắm tại L'Hex Shop!\n" +
					"Vui lòng liên hệ hỗ trợ qua email customers@lhex.vn hoặc hotline  0378 447 716.")
					.setFont(font)
					.setFontSize(10)
					.setTextAlignment(TextAlignment.CENTER)
					.setFontColor(ColorConstants.DARK_GRAY)
					.setMarginTop(10);
			document.add(footer);

			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Lỗi tạo PDF", e);
		}
	}
    public Page<ProductDTO> getBestSellingProducts(Pageable pageable) {
        Page<Object[]> results = orderRepository.findBestSellingProducts(pageable);
        return results.map(result -> {
            ProductEntity product = (ProductEntity) result[0];
            Long totalSold = (Long) result[1];

            ProductDTO dto = new ProductDTO();
            dto.setProductId(product.getProductId());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setDateCreated(product.getDateCreated());
            dto.setStatus(product.getStatus());
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getCategoryName());
            dto.setViewCount(totalSold.intValue()); // tạm dùng viewCount làm totalSold

            List<ProductVariantDTO> variants = product.getVariants().stream()
                .map(v -> {
                    ProductVariantDTO variantDTO = new ProductVariantDTO();
                    variantDTO.setProductVariantId(v.getProductVariantId());
                    variantDTO.setStock(v.getStock());
                    variantDTO.setPrice(v.getPrice());
                    variantDTO.setImageName(v.getImageName());
                    variantDTO.setColorId(v.getColor().getColorId());
                    variantDTO.setColorName(v.getColor().getColorName());
                    variantDTO.setSizeId(v.getSize().getSizeId());
                    variantDTO.setSizeName(v.getSize().getSizeName());
                    return variantDTO;
                })
                .collect(Collectors.toList());

            dto.setVariants(variants);
            return dto;
        });
    }

	@Transactional
	public void requestReturn(Integer orderId, OrderReturnDTO dto) {
		Integer userId = getAuthenticatedUserId();
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		if (!order.getUser().getUserId().equals(userId)) {
			throw new SecurityException("Không có quyền gửi yêu cầu trả hàng cho đơn này");
		}

		if (order.getStatus() != 3) {
			throw new IllegalStateException("Chỉ có thể yêu cầu trả hàng khi đơn đã giao");
		}

		if (jpaOrderReturnEntity.findByOrder(order).isPresent()) {
			throw new IllegalStateException("Đơn hàng đã gửi yêu cầu trả hàng");
		}

		OrderReturnEntity returnRequest = new OrderReturnEntity();
		returnRequest.setOrder(order);
		returnRequest.setUser(order.getUser());
		returnRequest.setReason(dto.getReason());
		returnRequest.setImageUrls(convertListToJson(dto.getImageUrls()));
		returnRequest.setVideoUrls(convertListToJson(dto.getVideoUrls()));
		returnRequest.setStatus(0);

		jpaOrderReturnEntity.save(returnRequest);

		order.setStatus(4);
		orderRepository.save(order);
	}

	private String convertListToJson(List<String> list) {
		return list != null ? new Gson().toJson(list) : "[]";
	}
	@Transactional
	public void acceptReturn(Integer orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		OrderReturnEntity returnRequest = jpaOrderReturnEntity.findByOrder(order)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu trả hàng"));

		if (order.getStatus() != 4 || returnRequest.getStatus() != 0) {
			throw new IllegalStateException("Yêu cầu không hợp lệ hoặc đã xử lý");
		}

		returnRequest.setStatus(1); // Chấp nhận
		jpaOrderReturnEntity.save(returnRequest);

		order.setStatus(6); // Trả hàng thành công
		order.setPaymentStatus(0); // Hoàn tiền
		adjustStockForOrder(order, true); // Trả lại hàng vào kho
		orderRepository.save(order);
	}

	@Transactional
	public void rejectReturn(Integer orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		OrderReturnEntity returnRequest = jpaOrderReturnEntity.findByOrder(order)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu trả hàng"));

		if (order.getStatus() != 4 || returnRequest.getStatus() != 0) {
			throw new IllegalStateException("Yêu cầu không hợp lệ hoặc đã xử lý");
		}

		returnRequest.setStatus(2); // Từ chối
		jpaOrderReturnEntity.save(returnRequest);

		order.setStatus(3); // Trả lại trạng thái "đã giao"
		orderRepository.save(order);
	}
	public OrderReturnDTO getReturnRequestByOrderId(Integer orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));

		OrderReturnEntity returnRequest = jpaOrderReturnEntity.findByOrder(order)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu trả hàng"));

		OrderReturnDTO dto = new OrderReturnDTO();
		dto.setReason(returnRequest.getReason());
		dto.setImageUrls(convertJsonToList(returnRequest.getImageUrls()));
		dto.setVideoUrls(convertJsonToList(returnRequest.getVideoUrls()));
		return dto;
	}

	private List<String> convertJsonToList(String json) {
		return json != null ? new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType()) : new ArrayList<>();
	}


	private List<String> parseJsonToList(String json) {
		return new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
	}

	public Long getTotalSoldQuantityByProductId(Integer productId) {
		Long totalSold = jpaOrderDetail.getTotalSoldQuantityByProductId(productId);
		return totalSold != null ? totalSold : 0L;
	}

	public OrderEntity findByTxnRef(String txnRef) {
		return orderRepository.findByTxnRef(txnRef)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã giao dịch: " + txnRef));
	}


	}
