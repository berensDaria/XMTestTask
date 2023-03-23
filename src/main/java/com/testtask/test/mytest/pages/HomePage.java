package com.testtask.test.mytest.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.testtask.test.mytest.enums.SliderDatePeriods;

public class HomePage {
    private static final String URL = "https://xm.com";
    private static final By MENU_BUTTON = By.xpath("//span[text()='Menu']//parent::button");
    private static final By ACCEPT_COOKIES_BUTTON = By.xpath("//button[text()='ACCEPT ALL']");
    private static final By RESEARCH_AND_EDUCATION_X_PATH = By.xpath("//li[contains(@class, 'main_nav_research')]");
    private static final By RESEARCH_AND_EDUCATION_SMALL_X_PATH = By.xpath("//li/a[@aria-controls='researchMenu']");
    private static final By REASEACH_AND_EDUCATION_MENU = By.xpath("//li[contains(@class, 'main_nav_research')]/div[@class='dropdown']");
    private static final By REASEACH_AND_EDUCATION_MENU_SMALL = By.xpath("//li/a[@aria-controls='researchMenu']/following-sibling::div");
    private static final String MENU_RESEARCH_ITEMS = "//li/a[contains(.,'%s')]";
    private static final By CALENDAR_AREA = By.xpath("//iframe[contains(@src,'calendar')]");
    private static final By ECONOMIC_CALENDAR_TAB = By.xpath("//div[contains(@class,'navigation')]//a[text()='Economic Calendar']");
    private static final String CALENDAR_LANDING = "//tc-economic-calendar-landing";
    private static final By LOAD_BUTTON = By.xpath("//div[@id='economic-calendar-list']/div[contains(@class,'load')]");
    private static final By SLIDER_TITLE = By.xpath(CALENDAR_LANDING + "//span[@class='tc-finalval-tmz']/div");
    private static final By SLIDER_TIME_LINE = By.xpath(CALENDAR_LANDING + "//mat-slider");
    private static final By CALENDAR_ICON = By.xpath(CALENDAR_LANDING + "//mat-icon");
    private static final By SELECTED_DAY = By.xpath("//tc-calendar//div[contains(@class,'selected')]/parent::button");
    private static final By CALENDAR_DAY = By.xpath("//tc-calendar//button[contains(@class, 'mat-calendar-body-cell')]");
    private static final By NEXT_BUTTON = By.xpath("//button[@aria-label='Next month']");
    private static final String STRONG_DISCLAIMER = "//strong[(.)='Disclaimer:']";
    private static final By DISCLAIMER = By.xpath(STRONG_DISCLAIMER);
    private static final By DISCLAIMER_HERE = By.xpath(STRONG_DISCLAIMER + "/parent::p/following-sibling::p/a[.='here']");
    private static final By RISC_WARING_HERE = By.xpath("//h5[(.)='Risk Warning']/following-sibling::p/a[.='here']");
    private static final String ARIA_LABEL = "aria-label";

    private final WebDriver driver;
    private final JavascriptExecutor js;
    private final Wait<WebDriver> wait;

    public HomePage(RemoteWebDriver driver) {
        this.driver = driver;
        this.js = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void open() {
        driver.get(URL);
        List<WebElement> acceptButtons = driver.findElements(ACCEPT_COOKIES_BUTTON);
        if (!acceptButtons.isEmpty()) {
            acceptButtons.get(0).click();
        }
    }

    public void openReaseachAndEducationMenu() {
        WebElement menuButton = driver.findElement(MENU_BUTTON);
        WebElement researchAndEducationMenuElement;
        if (menuButton.isDisplayed()) {
            menuButton.click();
            researchAndEducationMenuElement = driver.findElement(RESEARCH_AND_EDUCATION_SMALL_X_PATH);
        } else {
            researchAndEducationMenuElement = driver.findElement(RESEARCH_AND_EDUCATION_X_PATH);
        }
        scrollToElement(researchAndEducationMenuElement);
        researchAndEducationMenuElement.click();
    }

    public boolean isOpenReaseachAndEducationMenu() {
        return driver.findElement(REASEACH_AND_EDUCATION_MENU).isDisplayed() || driver.findElement(REASEACH_AND_EDUCATION_MENU_SMALL).isDisplayed();
    }

    public void openResearchByItemName(String itemName) {
        List<WebElement> researchItems = driver.findElements(By.xpath(String.format(MENU_RESEARCH_ITEMS, itemName)));
        if (researchItems.isEmpty()) {
            throw new IllegalArgumentException("Item " + itemName + " is absent on Reasearch menu");
        }
        int index = 0;
        if (!researchItems.get(0).isDisplayed()) {
            index = 1;
        }
        scrollToElement(researchItems.get(index));
        researchItems.get(index).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(CALENDAR_AREA));
    }

    public boolean isOpenEconomicCalendarTab() {
        return driver.findElement(ECONOMIC_CALENDAR_TAB).getAttribute("class").contains("active");
    }

    public void navigateSliderOnDatePeriod(SliderDatePeriods sliderDatePeriods) {
        int dateCode = sliderDatePeriods.getCode();
        if (!driver.findElement(SLIDER_TIME_LINE).isDisplayed()) {
            driver.findElement(CALENDAR_ICON).click();
        }
        scrollToElement(driver.findElement(By.xpath(CALENDAR_LANDING)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(SLIDER_TIME_LINE));
        WebElement timeLine = driver.findElement(SLIDER_TIME_LINE);
        timeLine.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOAD_BUTTON));
        int ariaValueNow = Integer.parseInt(timeLine.getAttribute("aria-valuenow"));
        if (ariaValueNow < dateCode) {
            IntStream.range(ariaValueNow, dateCode).forEach(i -> timeLine.sendKeys(Keys.ARROW_RIGHT));
        } else if (ariaValueNow > dateCode) {
            IntStream.range(dateCode, ariaValueNow).forEach(i -> timeLine.sendKeys(Keys.ARROW_LEFT));
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOAD_BUTTON));
    }

    public List<String> getSelectedDates() {
        List<WebElement> selectedDays = driver.findElements(SELECTED_DAY);
        if (selectedDays.size() >= 2 && !selectedDays.get(0).getAttribute(ARIA_LABEL).equals(selectedDays.get(1).getAttribute(ARIA_LABEL))) {
            return List.of(selectedDays.get(0).getAttribute(ARIA_LABEL), selectedDays.get(1).getAttribute(ARIA_LABEL));
        }
        List<WebElement> calendarDaysElements = driver.findElements(CALENDAR_DAY);
        if (calendarDaysElements.get(calendarDaysElements.size() - 1).getAttribute("class").contains("in-range")) {
            List<String> selectedDates = new ArrayList<>(List.of(selectedDays.get(0).getAttribute(ARIA_LABEL)));
            driver.findElement(NEXT_BUTTON).click();
            selectedDates.add(driver.findElements(SELECTED_DAY).get(0).getAttribute(ARIA_LABEL));
            return selectedDates;
        }
        return List.of(selectedDays.get(0).getAttribute(ARIA_LABEL));
    }

    public String getSliderTitle() {
        String title;
        try {
            title = driver.findElement(SLIDER_TITLE).getText();
        } catch (StaleElementReferenceException e) { // happens often due to page refreshes
            title = driver.findElement(SLIDER_TITLE).getText();
        }
        return title;
    }

    public void openDisclaimerHere() {
        scrollToElement(driver.findElement(DISCLAIMER));
        WebElement disclamerHereElement = driver.findElement(DISCLAIMER_HERE);
        try {
            disclamerHereElement.click();
        } catch (ElementClickInterceptedException e) {
            scrollToElement(disclamerHereElement);
            disclamerHereElement.click();
        }
    }

    public void openRiskWarningHere() {
        WebElement riskWarningHereElement = driver.findElement(RISC_WARING_HERE);
        scrollToElement(riskWarningHereElement);
        riskWarningHereElement.click();
    }

    private void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView();", element);
    }
}
