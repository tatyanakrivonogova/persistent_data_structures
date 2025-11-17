import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorTest {

    @Test
    void sumWorks() {
        int result = 2 + 2;
        int expected = 4;
        assertEquals(expected, result);
    }
}
