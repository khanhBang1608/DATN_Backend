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
import com.java.fashionshop.config.GhnConfig;
import com.java.fashionshop.dto.*;
import com.java.fashionshop.entity.*;
import com.java.fashionshop.helper.OrderHelperClass;
import com.java.fashionshop.jpa.*;
import com.java.fashionshop.request.OrderCreateRequest;
import com.java.fashionshop.respone.GhnOrderStatusResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

	@Autowired
	private JpaAddress jpaAddress;

	@Autowired
	private GhnService ghnService;

	@Autowired
	private GhnConfig ghnConfig;

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
		if (request.getAddressId() == null) {
			throw new IllegalArgumentException("addressId là bắt buộc");
		}
		OrderEntity order = new OrderEntity();
		order.setUser(user);
		order.setOrderDate(LocalDateTime.now());
		order.setAddress(request.getAddress());
		order.setAddressId(request.getAddressId()); // Lưu addressId
		order.setIdempotencyKey(request.getIdempotencyKey());
		order.setPaymentMethod(request.getPaymentMethod());
		order.setStatus(0); // 0: Chờ xác nhận
		if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
			order.setPaymentStatus(1); // 1: Paid
		} else {
			order.setPaymentStatus(0); // 0: Unpaid
		}
		order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
		order.setDiscountAmount(BigDecimal.ZERO);

		if (request.getDiscountCode() != null) {
			DiscountEntity discount = discountRepository.findByDiscountCode(request.getDiscountCode()).orElse(null);
			if (discount != null && discount.getStatus() && discount.getEndDate().isAfter(LocalDateTime.now())) {
				if (discount.getQuantityLimit() != null && discount.getQuantityLimit() > 0) {
					discount.setQuantityLimit(discount.getQuantityLimit() - 1);
					discountRepository.save(discount);
					order.setDiscountCode(discount.getDiscountCode());
					BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
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
				price = variant.getPrice();
			}

			OrderDetailEntity detail = new OrderDetailEntity();
			detail.setOrder(order);
			detail.setQuantity(detailRequest.getQuantity());
			detail.setPrice(price);
			detail.setProductVariant(variant);

			order.getOrderDetails().add(detail);
			totalAmount = totalAmount.add(price.multiply(new BigDecimal(detail.getQuantity())));
		}
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
		else if (previousStatus == 3 && orderDTO.getStatus() == 6) {
			adjustStockForOrder(order, true);
			order.setPaymentStatus(2);
		}

//		if (orderDTO.getStatus() == 2 && previousStatus != 2) {
//			// Chuyển sang đang giao hàng => trừ stock
//			adjustStockForOrder(order, false);
//		}

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

		String ghnOrderStatus = order.getGhnOrderStatus();
		String ghnOrderCode = order.getGhnOrderCode();
		if (order.getGhnOrderCode() != null && ghnOrderStatus == null) {
			try {
				ghnOrderStatus = ghnService.getOrderStatus(order.getGhnOrderCode()).getData().getStatus();
			} catch (Exception e) {
				ghnOrderStatus = "Không thể lấy trạng thái GHN";
			}
		}

		OrderDTO orderDTO = new OrderDTO(
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
				order.getUser().getFullName(),
				ghnOrderStatus,
				ghnOrderCode
		);
		orderDTO.setGhnOrderCode(order.getGhnOrderCode()); // Thêm dòng này
		return orderDTO;
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
		String[] addressParts = order.getAddress().split(" - ", 3); // Split into 3 parts
		String fullName = addressParts.length > 0 ? addressParts[0].trim() : "";
		String phoneNumber = addressParts.length > 1 ? addressParts[1].trim() : "";
		String address = addressParts.length > 2 ? addressParts[2].trim() : "";
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

			document.add(new Paragraph("CÔNG TY CỔ PHẦN L'HEX INTERNATIONAL FASHION TRADING")
					.setFont(boldFont)
					.setFontSize(12)
					.setTextAlignment(TextAlignment.LEFT));
			document.add(new Paragraph("Địa chỉ: Toà nhà FPT Polytechnic, Đ. Số 22, Thường Thạnh, Cái Răng, Cần Thơ")
					.setFont(font)
					.setFontSize(10));
			document.add(new Paragraph("Số điện thoại:  0378 447 716 | Email: Bytecrew@lhex.vn")
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
					.add(new Paragraph("Người đặt: " + order.getUser().getFullName()).setFont(boldFont).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Ngày đặt hàng: " + ((LocalDateTime) order.getOrderDate()).format(dateTimeFormatter))
							.setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Người nhận: " + fullName).setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Phương thức thanh toán: " + order.getPaymentMethod()).setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Số điện thoại người nhận: " + phoneNumber).setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Trạng thái thanh toán: " + (order.getPaymentStatus() == 1 ? "Đã thanh toán" : "Chưa thanh toán"))
							.setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("Địa chỉ người nhận: " + address).setFont(font).setFontSize(10)));
			infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)
					.add(new Paragraph("")));
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
					"Vui lòng liên hệ hỗ trợ qua email Bytecrew@lhex.vn hoặc hotline  0378 447 716.")
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
		order.setPaymentStatus(2); // 2 = Hoàn tiền
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

		order.setStatus(7); // Trả lại trạng thái "từ chối trả hàng"
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
	public Page<OrderDTO> getOrdersByUserId(Integer userId, Pageable pageable) {
	    return orderRepository.findByUser_UserId(userId, pageable)
	            .map(this::convertToDTO);
	}


	// GHN order
	@Transactional
	public OrderDTO approveOrder(Integer orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		if (order.getStatus() != 0) {
			throw new IllegalStateException("Chỉ có thể duyệt đơn hàng ở trạng thái Chờ xác nhận");
		}

		// Tạo request cho createGhnOrder
		OrderCreateRequest request = new OrderCreateRequest();
		request.setAddress(order.getAddress());
		request.setAddressId(order.getAddressId()); // Sử dụng addressId từ OrderEntity
		request.setPaymentMethod(order.getPaymentMethod());
		request.setShippingFee(order.getShippingFee());
		request.setDiscountCode(order.getDiscountCode());
		request.setDiscountAmount(order.getDiscountAmount());
		List<OrderCreateRequest.OrderDetailRequest> orderDetails = order.getOrderDetails().stream()
				.map(detail -> new OrderCreateRequest.OrderDetailRequest(
						detail.getProductVariant().getProductVariantId(),
						detail.getQuantity(),
						detail.getPrice()
				))
				.collect(Collectors.toList());
		request.setOrderDetails(orderDetails);

		// Tạo đơn hàng trên GHN
		String ghnOrderCode = createGhnOrder(order, request);
		order.setGhnOrderCode(ghnOrderCode);
		order.setStatus(1); // Chuyển sang Chờ lấy hàng
		order = orderRepository.save(order);

		// Đồng bộ trạng thái sau khi duyệt
		syncGhnOrderStatus(order.getOrderId());

		return convertToDTO(order);
	}

	private String createGhnOrder(OrderEntity order, OrderCreateRequest request) {
		RestTemplate restTemplate = new RestTemplate();
		String url = ghnConfig.getUrl() + "/shipping-order/create";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Token", ghnConfig.getToken());
		headers.set("Content-Type", "application/json");

		if (request.getAddressId() == null) {
			throw new IllegalArgumentException("addressId là bắt buộc trong createGhnOrder");
		}
		AddressEntity address = jpaAddress.findById(request.getAddressId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + request.getAddressId()));
		Integer toDistrictId = address.getDistrictId();
		String wardCode = String.valueOf(address.getWardId());

		// Lấy service_id từ API GHN
		String serviceUrl = ghnConfig.getUrl() + "/shipping-order/available-services";
		Map<String, Object> serviceBody = new HashMap<>();
		serviceBody.put("shop_id", Integer.valueOf(ghnConfig.getShopId()));
		serviceBody.put("from_district", Integer.valueOf(ghnConfig.getFromDistrictId()));
		serviceBody.put("to_district", toDistrictId);

		HttpEntity<Map<String, Object>> serviceRequest = new HttpEntity<>(serviceBody, headers);
		ResponseEntity<Map> serviceResponse = restTemplate.exchange(serviceUrl, HttpMethod.POST, serviceRequest, Map.class);

		Map<String, Object> serviceResponseBody = serviceResponse.getBody();
		if (serviceResponseBody == null || !serviceResponseBody.containsKey("data")) {
			throw new RuntimeException("Không tìm thấy dịch vụ giao hàng phù hợp.");
		}

		List<Map<String, Object>> data = (List<Map<String, Object>>) serviceResponseBody.get("data");
		if (data.isEmpty()) {
			throw new RuntimeException("Danh sách dịch vụ rỗng.");
		}

		Integer serviceId = (Integer) data.get(0).get("service_id");
		if (serviceId == null) {
			throw new RuntimeException("Không lấy được service_id.");
		}

		// Tạo request body cho API tạo đơn hàng GHN
		Map<String, Object> ghnRequest = new HashMap<>();
		ghnRequest.put("shop_id", Integer.valueOf(ghnConfig.getShopId()));
		ghnRequest.put("from_district_id", Integer.valueOf(ghnConfig.getFromDistrictId()));
		ghnRequest.put("to_district_id", toDistrictId);
		ghnRequest.put("to_ward_code", wardCode);
		ghnRequest.put("payment_type_id", 2); // 2: Người nhận trả phí
		ghnRequest.put("note", "Đơn hàng từ hệ thống");
		ghnRequest.put("required_note", "KHONGCHOXEMHANG");

		// Thông tin người nhận từ order.address
		String[] addressParts = order.getAddress().split(" - ", 3);
		ghnRequest.put("to_name", addressParts.length > 0 ? addressParts[0].trim() : "");
		ghnRequest.put("to_phone", addressParts.length > 1 ? addressParts[1].trim() : "");
		ghnRequest.put("to_address", addressParts.length > 2 ? addressParts[2].trim() : "");

		// Chi tiết sản phẩm
		List<Map<String, Object>> items = new ArrayList<>();
		for (OrderDetailEntity detail : order.getOrderDetails()) {
			Map<String, Object> item = new HashMap<>();
			item.put("name", detail.getProductVariant().getProduct().getName());
			item.put("quantity", detail.getQuantity());
			item.put("price", detail.getPrice().intValue());
			item.put("weight", 100);
			items.add(item);
		}
		ghnRequest.put("items", items);

		// Các thông số khác
		ghnRequest.put("weight", 1000);
		ghnRequest.put("length", 30);
		ghnRequest.put("width", 20);
		ghnRequest.put("height", 10);
		ghnRequest.put("service_id", serviceId);

		HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(ghnRequest, headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
			Map<String, Object> responseBody = response.getBody();
			if (responseBody != null && responseBody.containsKey("data")) {
				Map<String, Object> dataResponse = (Map<String, Object>) responseBody.get("data");
				String ghnOrderCode = (String) dataResponse.get("order_code");
				order.setGhnOrderCode(ghnOrderCode);
				orderRepository.save(order);
				return ghnOrderCode;
			}
			throw new RuntimeException("Không thể tạo đơn hàng GHN");
		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi gọi API GHN tạo đơn hàng: " + e.getMessage());
		}
	}

	public String getGhnOrderStatus(Integer orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		if (order.getGhnOrderCode() == null) {
			throw new IllegalStateException("Đơn hàng chưa được gửi qua GHN");
		}

		GhnOrderStatusResponse response = ghnService.getOrderStatus(order.getGhnOrderCode());
		return response.getData().getStatus();
	}

	@Transactional
	public void syncGhnOrderStatus(Integer orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

		if (order.getGhnOrderCode() == null) {
			throw new IllegalStateException("Đơn hàng chưa được gửi qua GHN");
		}

		// Bỏ qua đồng bộ nếu đơn hàng ở trạng thái Chờ xác nhận
		if (order.getStatus() == 0) {
			System.out.println("Bỏ qua đồng bộ vì đơn hàng đang ở trạng thái Chờ xác nhận: " + order.getStatus());
			return;
		}

		GhnOrderStatusResponse response = ghnService.getOrderStatus(order.getGhnOrderCode());
		String ghnStatus = response.getData().getStatus();

		System.out.println("GHN trả về status: " + ghnStatus);
		System.out.println("Status hiện tại trong DB: " + order.getStatus());

		// Kiểm tra trạng thái trả hàng
		if (order.getStatus() == 4 || order.getStatus() == 6 || order.getStatus() == 7) {
			if (order.getStatus() == 4 && "return".equals(ghnStatus)) {
				order.setStatus(6); // Trả hàng thành công
				order.setGhnOrderStatus(ghnStatus);
				order.setPaymentStatus(2); // Hoàn tiền
				adjustStockForOrder(order, true); // Trả lại hàng vào kho
				orderRepository.save(order);
				System.out.println("Cập nhật trạng thái trả hàng thành công: " + order.getStatus());
			} else {
				System.out.println("Bỏ qua đồng bộ vì đơn hàng đang ở trạng thái trả hàng: " + order.getStatus());
			}
			return;
		}

		Integer newStatus = mapGhnStatusToInternalStatus(ghnStatus);

		if (!newStatus.equals(order.getStatus()) || !ghnStatus.equals(order.getGhnOrderStatus())) {
			order.setStatus(newStatus);
			order.setGhnOrderStatus(ghnStatus);
			if (newStatus == 3) {
				order.setPaymentStatus(1); // Đã thanh toán
			} else if (newStatus == 5) {
				order.setPaymentStatus(0); // Chưa thanh toán
				adjustStockForOrder(order, true); // Hoàn lại hàng nếu cần
			}
			orderRepository.save(order);
			System.out.println("Cập nhật thành công sang status mới: " + newStatus);
		} else {
			System.out.println("Không có thay đổi trạng thái, không cần update.");
		}
	}

	private Integer mapGhnStatusToInternalStatus(String ghnStatus) {
		switch (ghnStatus) {
			case "ready_to_pick":
			case "picking":
				return 1; // Chuẩn bị hàng
			case "delivering":
				return 2; // Đang giao hàng
			case "delivered":
				return 3; // Đã giao
			case "cancel":
				return 5; // Đã hủy
			case "return":
				return 6; // Trả hàng thành công
			default:
				return 0; // Pending
		}
	}

	public boolean isOrderReturnRejected(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        return jpaOrderReturnEntity.findByOrder(order)
                .map(returnRequest -> returnRequest.getStatus() == 2)
                .orElse(false);
    }
}
