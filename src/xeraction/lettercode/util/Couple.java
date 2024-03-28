package xeraction.lettercode.util;

/**
 * A very simple class for coupling two values together
 * @param first The first value
 * @param second The second value
 * @param <T> The type of the first value
 * @param <S> The type of the second value
 */
public record Couple<T, S>(T first, S second) {}
