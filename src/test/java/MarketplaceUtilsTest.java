import com.marketplace.util.MarketplaceUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты бизнес-логики маркетплейса")
public class MarketplaceUtilsTest {

    // ─────────────────────────────────────────────────────────
    // Расчёт скидки промокода
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Процентная скидка 10% от 1000 ₽ = 100 ₽")
    void percentDiscount_tenPercentOf1000_returns100() {
        BigDecimal result = MarketplaceUtils.calculateDiscount(
                new BigDecimal("1000"), "percent", new BigDecimal("10"));
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    @DisplayName("Фиксированная скидка 200 ₽ от 1000 ₽ = 200 ₽")
    void fixedDiscount_200from1000_returns200() {
        BigDecimal result = MarketplaceUtils.calculateDiscount(
                new BigDecimal("1000"), "fixed", new BigDecimal("200"));
        assertEquals(new BigDecimal("200"), result);
    }

    @Test
    @DisplayName("Фиксированная скидка не может превысить сумму заказа")
    void fixedDiscount_biggerThanOrder_cappedAtOrderAmount() {
        BigDecimal result = MarketplaceUtils.calculateDiscount(
                new BigDecimal("500"), "fixed", new BigDecimal("1000"));
        assertEquals(new BigDecimal("500"), result);
    }

    // ─────────────────────────────────────────────────────────
    // Применение скидки
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("1000 ₽ − скидка 200 ₽ = итого 800 ₽")
    void applyDiscount_1000minus200_returns800() {
        BigDecimal result = MarketplaceUtils.applyDiscount(
                new BigDecimal("1000"), new BigDecimal("200"));
        assertEquals(new BigDecimal("800"), result);
    }

    @Test
    @DisplayName("Итоговая сумма не может быть отрицательной")
    void applyDiscount_discountBiggerThanOrder_returnsZero() {
        BigDecimal result = MarketplaceUtils.applyDiscount(
                new BigDecimal("100"), new BigDecimal("500"));
        assertEquals(BigDecimal.ZERO, result);
    }

    // ─────────────────────────────────────────────────────────
    // Лимит использований промокода
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Промокод исчерпан: использований = лимиту")
    void promoExhausted_usedEqualsMax_returnsTrue() {
        assertTrue(MarketplaceUtils.isPromoCodeExhausted(10, 10));
    }

    @Test
    @DisplayName("Промокод не исчерпан: использований меньше лимита")
    void promoNotExhausted_usedLessThanMax_returnsFalse() {
        assertFalse(MarketplaceUtils.isPromoCodeExhausted(10, 5));
    }

    @Test
    @DisplayName("Промокод без лимита (maxUses=0) всегда доступен")
    void promoUnlimited_maxUsesZero_returnsFalse() {
        assertFalse(MarketplaceUtils.isPromoCodeExhausted(0, 999));
    }

    // ─────────────────────────────────────────────────────────
    // Срок действия промокода
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Срок действия промокода не истёк (будущая дата)")
    void promoNotExpired_futureDateValid() {
        String future = LocalDate.now().plusDays(30).toString();
        assertFalse(MarketplaceUtils.isPromoCodeExpired(future));
    }

    @Test
    @DisplayName("Срок действия промокода истёк (прошлая дата)")
    void promoExpired_pastDateExpired() {
        String past = "2020-01-01";
        assertTrue(MarketplaceUtils.isPromoCodeExpired(past));
    }

    // ─────────────────────────────────────────────────────────
    // Дата доставки
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Дата доставки: сегодня + 7 дней корректна")
    void estimatedDelivery_sevenDaysFromNow_correctDate() {
        String expected = LocalDate.now().plusDays(7).toString();
        assertEquals(expected, MarketplaceUtils.calculateEstimatedDelivery(7));
    }

    // ─────────────────────────────────────────────────────────
    // Стоимость позиции корзины
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Стоимость позиции: 500 ₽ × 3 шт = 1500 ₽")
    void cartItemTotal_500priceAnd3qty_returns1500() {
        BigDecimal result = MarketplaceUtils.calculateCartItemTotal(
                new BigDecimal("500"), 3);
        assertEquals(new BigDecimal("1500"), result);
    }

    @Test
    @DisplayName("Стоимость позиции при нулевом количестве = 0")
    void cartItemTotal_zeroQuantity_returnsZero() {
        BigDecimal result = MarketplaceUtils.calculateCartItemTotal(
                new BigDecimal("500"), 0);
        assertEquals(BigDecimal.ZERO, result);
    }

    // ─────────────────────────────────────────────────────────
    // Минимальная сумма заказа для промокода
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Сумма заказа достаточна для промокода")
    void orderSufficient_amountMeetsMinimum_returnsTrue() {
        assertTrue(MarketplaceUtils.isOrderAmountSufficient(
                new BigDecimal("1500"), new BigDecimal("1000")));
    }

    @Test
    @DisplayName("Сумма заказа недостаточна для промокода")
    void orderNotSufficient_amountBelowMinimum_returnsFalse() {
        assertFalse(MarketplaceUtils.isOrderAmountSufficient(
                new BigDecimal("500"), new BigDecimal("1000")));
    }
}
