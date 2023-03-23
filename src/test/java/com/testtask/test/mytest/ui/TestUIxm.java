package com.testtask.test.mytest.ui;

import static java.lang.Integer.parseInt;
import static java.util.List.of;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import com.testtask.test.mytest.enums.SliderDatePeriods;
import com.testtask.test.mytest.pages.HomePage;
import io.github.bonigarcia.wdm.WebDriverManager;

@SpringBootTest
class TestUIxm {

    private static final String MAXIMUM = "maximum";
    private WebDriver driver;
    private HomePage homePage;

    @BeforeAll
    static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*"); // https://stackoverflow.com/questions/75680149/unable-to-establish-websocket-connection
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        ChromeDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        driver = chromeDriver;
        homePage = new HomePage(chromeDriver);
    }

    @AfterEach
    void teardown() {
        driver.quit();
    }

    /* UI Test task
    Repeat for different browser’s screen resolution
    1)	Maximum (supported by your display)
    2)	1024 x 768
    3)	800 x 600
    Steps:
    1. Open Home page (make any check here if needed).
    2. Click the <Research and Education> link located at the top menu (make any check here if needed).
    3. Click <Economic Calendar> link in the opened menu (make any check here if needed).
    4. Select <Today> on Slider and check that the date is correct.
    5. Select <Tomorrow> on Slider and check that the date is correct.
    6. Select <Next Week> on Slider and check that the date is correct.
    7. Select <Next Month> on Slider and check that the date is correct.
    8. Click <here> link in the “disclaimer” block at the bottom (make any check here if needed).
    9. Click <here> link in the “Risk Warning” block at the bottom.
    10. Check that <Risk Disclosure> document was opened in new tab.
     */
    @ParameterizedTest
    @CsvSource({"maximum,maximum", "1024,768", "800,600"})
    @Execution(ExecutionMode.CONCURRENT)
    void uiTest(String width, String height) {
        if (width.equalsIgnoreCase(MAXIMUM) && height.equalsIgnoreCase(MAXIMUM)) {
            driver.manage().window().maximize();
        } else if (isNumeric(width) && isNumeric(height)) {
            driver.manage().window().setSize(new Dimension(parseInt(width), parseInt(height)));
        } else {
            throw new IllegalArgumentException("Unsupported combination of widht " + width + " or height " + height);
        }

        // Opened Home page and checked title.
        homePage.open();
        assertThat(driver.getTitle()).contains("Forex & CFD Trading on Stocks, Indices, Oil, Gold by XM");
        // Clicked the <Research and Education> link located at the top menu and checked then this menu is opened.
        homePage.openReaseachAndEducationMenu();
        assertThat(homePage.isOpenReaseachAndEducationMenu()).isTrue();
        //  Click <Economic Calendar> link in the opened menu and checked that this Tab is opend
        homePage.openResearchByItemName("Economic Calendar");
        assertThat(homePage.isOpenEconomicCalendarTab()).isTrue();

        // swithched to Slider area
        SoftAssertions softly = new SoftAssertions();
        driver.switchTo().frame(0);

        // Selected <Today> on Slider and check that the date is correct.
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEE MMM dd yyyy");
        LocalDate now = LocalDate.now();
        homePage.navigateSliderOnDatePeriod(SliderDatePeriods.TODAY);
        softly.assertThat(homePage.getSliderTitle()).isEqualToIgnoringCase(SliderDatePeriods.TODAY.getName());
        softly.assertThat(homePage.getSelectedDates().get(0)).isEqualTo(dateFormat.format(now));

        // Selected <Tomorrow> on Slider and check that the date is correct.
        homePage.navigateSliderOnDatePeriod(SliderDatePeriods.TOMORROW);
        softly.assertThat(homePage.getSliderTitle()).isEqualToIgnoringCase(SliderDatePeriods.TOMORROW.getName());
        softly.assertThat(homePage.getSelectedDates().get(0)).isEqualTo(dateFormat.format(now.plusDays(1)));

        // Selected <Next Week> on Slider and check that the date is correct.
        homePage.navigateSliderOnDatePeriod(SliderDatePeriods.NEXT_WEEK);
        softly.assertThat(homePage.getSliderTitle()).isEqualToIgnoringCase(SliderDatePeriods.NEXT_WEEK.getName());
        LocalDate firstDayOfNextWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).plusWeeks(1);
        LocalDate lastDayOfNextWeek = now.plusDays(7 - now.getDayOfWeek().getValue()).plusWeeks(1);
        softly.assertThat(homePage.getSelectedDates())
                .containsExactlyInAnyOrderElementsOf(of(dateFormat.format(firstDayOfNextWeek), dateFormat.format(lastDayOfNextWeek)));

        // Selected <Next Month> on Slider and check that the date is correct.
        homePage.navigateSliderOnDatePeriod(SliderDatePeriods.NEXT_MONTH);
        softly.assertThat(homePage.getSliderTitle()).isEqualToIgnoringCase(SliderDatePeriods.NEXT_MONTH.getName());
        LocalDate firstDayOfNextMonth = now.plusDays(now.lengthOfMonth() - now.getDayOfMonth() + 1);
        LocalDate lastDayOfNextMonth = firstDayOfNextMonth.plusDays(firstDayOfNextMonth.lengthOfMonth() - 1);
        softly.assertThat(homePage.getSelectedDates())
                .containsExactlyInAnyOrderElementsOf(of(dateFormat.format(firstDayOfNextMonth), dateFormat.format(lastDayOfNextMonth)));

        // returned to the main page
        driver.switchTo().parentFrame();
        softly.assertAll();

        // Clicked <here> link in the “disclaimer” block, checked that previous tab is not active and opened one window.
        homePage.openDisclaimerHere();
        assertThat(homePage.isOpenEconomicCalendarTab()).isFalse();
        assertThat(driver.getWindowHandles()).hasSize(1);
        // Clicked <here> link in the “Risk Warning”.
        homePage.openRiskWarningHere();
        // Checked that <Risk Disclosure> document was opened in new tab (checked that two windows are opened)
        assertThat(driver.getWindowHandles()).hasSize(2);
    }
}
