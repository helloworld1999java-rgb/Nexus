package com.marketplace.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Вспомогательный класс с чистой бизнес-логикой приложения.
 * Не зависит от JavaFX и HTTP — пригоден для JUnit-тестирования.
 */
public class MarketplaceUtils {

    private MarketplaceUtils() {}

    /**
     * Рассчитать сумму скидки по промокоду.
     *
     * @param orderAmount  сумма заказа
     * @param discountType тип скидки: "percent" или "fixed"
     * @param discountValue значение скидки
     * @return сумма скидки (не больше суммы заказа)
     */
    public static BigDecimal calculateDiscount(BigDecimal orderAmount,
                                               String discountType,
                                               BigDecimal discountValue) {
        if (orderAmount == null || discountType == null || discountValue == null)
            return BigDecimal.ZERO;
        if ("percent".equals(discountType)) {
            return orderAmount
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // fixed: скидка не может превышать сумму заказа
            return discountValue.min(orderAmount);
        }
    }

    /**
     * Применить скидку к сумме заказа.
     *
     * @param orderAmount    сумма заказа до скидки
     * @param discountAmount сумма скидки
     * @return итоговая сумма (минимум 0)
     */
    public static BigDecimal applyDiscount(BigDecimal orderAmount, BigDecimal discountAmount) {
        if (orderAmount == null) return BigDecimal.ZERO;
        if (discountAmount == null) return orderAmount;
        BigDecimal result = orderAmount.subtract(discountAmount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    /**
     * Проверить, достигнут ли лимит использований промокода.
     *
     * @param maxUses   максимальное количество использований (0 = без ограничения)
     * @param usedCount текущее количество использований
     * @return true, если лимит исчерпан
     */
    public static boolean isPromoCodeExhausted(int maxUses, int usedCount) {
        return maxUses > 0 && usedCount >= maxUses;
    }

    /**
     * Проверить, истёк ли срок действия промокода.
     *
     * @param validUntil дата окончания в формате "YYYY-MM-DD..." (или null)
     * @return true, если срок истёк
     */
    public static boolean isPromoCodeExpired(String validUntil) {
        if (validUntil == null || validUntil.isEmpty()) return false;
        LocalDate until = LocalDate.parse(validUntil.substring(0, 10));
        return LocalDate.now().isAfter(until);
    }

    /**
     * Рассчитать дату ожидаемой доставки.
     *
     * @param maxDeliveryDays максимальный срок доставки в днях
     * @return дата в формате ISO (YYYY-MM-DD)
     */
    public static String calculateEstimatedDelivery(int maxDeliveryDays) {
        return LocalDate.now().plusDays(maxDeliveryDays).toString();
    }

    /**
     * Рассчитать итоговую стоимость позиции корзины.
     *
     * @param price    цена единицы товара
     * @param quantity количество
     * @return стоимость позиции
     */
    public static BigDecimal calculateCartItemTotal(BigDecimal price, int quantity) {
        if (price == null || quantity <= 0) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Проверить, достаточна ли сумма заказа для применения промокода.
     *
     * @param orderAmount    сумма заказа
     * @param minOrderAmount минимальная сумма (null = без ограничения)
     * @return true, если сумма подходит
     */
    public static boolean isOrderAmountSufficient(BigDecimal orderAmount,
                                                   BigDecimal minOrderAmount) {
        if (minOrderAmount == null) return true;
        return orderAmount.compareTo(minOrderAmount) >= 0;
    }
}
