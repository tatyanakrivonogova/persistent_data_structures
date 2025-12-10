import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CalculatorTest {

  @Test
  void sumWorks() {
    int result = 2 + 2;
    int expected = 4;
    assertEquals(expected, result);
  }
}
