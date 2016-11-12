/**
 *  xmlium-web, is an extension for selenium-java test framework allowing for tests
 *  to be described in xml files.
 *
 *  The contents of this file are subject GNU Lesser General Public License
 *  Version 3 or later, you may not use this file except in compliance
 *  with the License.
 *
 *  You may obtain a copy of the License at:
 *  https://www.gnu.org/licenses/lgpl.html
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 */
package org.xmlium.test.web.commons.xml;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.xmlium.test.web.commons.SelectData;
import org.xmlium.testsuite.ByCSSSelector;
import org.xmlium.testsuite.ByXPath;
import org.xmlium.testsuite.CheckEqualsToString;
import org.xmlium.testsuite.Element;
import org.xmlium.testsuite.Find;
import org.xmlium.testsuite.Finds;
import org.xmlium.testsuite.Index;
import org.xmlium.testsuite.Middle;
import org.xmlium.testsuite.ObjectFactory;
import org.xmlium.testsuite.PrettySelect;
import org.xmlium.testsuite.PrettySelectBy;
import org.xmlium.testsuite.SelectBy;
import org.xmlium.testsuite.StepType;
import org.xmlium.testsuite.Steps;
import org.xmlium.testsuite.StepsType;
import org.xmlium.testsuite.SwitchTo;
import org.xmlium.testsuite.Values;
import org.xmlium.testsuite.WaitFor;

import com.google.common.base.Function;

@SuppressWarnings("restriction")
public class XMLTestSteps {
	private static final Logger logger = Logger.getLogger(XMLTestSteps.class.getSimpleName());
	private XMLTestSuite suite = null;
	private StepsType stepsObject;
	private List<StepType> steps = new ArrayList<StepType>();

	private static final Pattern propertiesStringPattern = Pattern.compile("properties:(.+)");
	private static final Pattern expressionStringPattern = Pattern.compile("expression:(.+)");
	protected static final Pattern xpathIndexStringPattern = Pattern.compile(".*(index:\\$\\{(.+)\\}).*");
	protected static final Pattern valueExpressionStringPattern1 = Pattern.compile("(\\d+)([\\+\\-\\*/])(\\$\\{0\\})");
	protected static final Pattern valueExpressionStringPattern2 = Pattern.compile("(\\$\\{0\\})([\\+\\-\\*/])(\\d+)");
	protected static final Pattern valueExpressionStringPattern3 = Pattern
			.compile("(\\$\\{\\d\\})([\\+\\-\\*/])(\\$\\{\\d\\})");

	private Map<String, Element> store = new HashMap<String, Element>();
	private Map<String, String> elementValuesMap = new HashMap<String, String>();

	private Integer xpathIndex = 0;

	public XMLTestSteps(XMLTestSuite suite, StepsType step) throws Exception{
		super();
		this.suite = suite;
		this.stepsObject = step;
		if(this.stepsObject.getSteps()!=null){
			initStepsFromFiles(this.stepsObject.getSteps());
		}
	} 
	
	protected void initFromFiles(List<Object> stepsObjects) throws Exception{
		for(Object o: stepsObjects){
			if(o instanceof StepType){
				steps.add((StepType)o);
			}else if(o instanceof String){
				loadStepsFromFile((String)o);
			}
		}
	}
	protected void initStepsFromFiles(List<Steps> stepsObjects) throws Exception{
		for(Steps o: stepsObjects){
			initFromFiles(o.getStepOrStepsFile());
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	void loadStepsFromFile(String xmlFileName) throws Exception {
		try {
			xmlFileName = unformatValue(xmlFileName);
			logger.debug("xmlFileName: " + "'"+xmlFileName+"'");
			InputStream is = getClass().getResourceAsStream(xmlFileName);
			if(is==null){
				throw new FileNotFoundException(xmlFileName+" not found!");
			}

			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);

	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        StepsType stepsTypeObject = ((JAXBElement<StepsType>)unmarshaller.unmarshal(is)).getValue();

			if(stepsTypeObject.getSteps()!=null){
				initStepsFromFiles(stepsTypeObject.getSteps());
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage(), e);
			throw e;
		} finally {
		}
	}
	

	void processSteps() throws Exception {
		if (steps == null || steps.size() == 0) {
			throw new RuntimeException("no steps defined!");
		}

		for (StepType step : steps) {
			logger.debug("Step: " + step);
			try {
				Thread.sleep(getSuite().getDelay());
			} catch (Exception e) {
				// TODO: handle exception
			}
			processStep(step);
		}

	}

	protected void processStep(StepType step) throws Exception {
		WebElement element = null;
		Alert a = null;
		if (step.getScrollX() != null && step.getScrollY() != null) {
			JavascriptExecutor js = (JavascriptExecutor) getSuite().getDriver();
			js.executeScript("window.scrollTo(" + step.getScrollX() + ", " + step.getScrollY() + ");");
		}
		Element e = null;
		if (step.getLoad() != null && step.getLoad().getKey() != null) {
			e = store.get(step.getLoad().getKey());
			element = findElement(e.getFinds());
		}
		if (step.getElement() != null) {
			if (step.getElement().getFinds() != null) {
				e = step.getElement();
				checkStore(e);

				element = checkWaitFor(e);
				if (element == null && step.getElement().isCheckNullElement()) {
					return;
				}
				checkStoreValue(e, element);
				checkSetValue(e, element);
				checkSendKeys(e, element);
				checkClick(e, element);
				checkChangeState(e);
				checkText(e, element);
				checkMoveX(e, element);
			}
		}
		if (step.getSwitchTo() != null) {
			Object to = switchTo(step.getSwitchTo());
			if (to instanceof Alert) {
				a = (Alert) to;
				if (step.getSwitchTo().getAlert() != null && step.getSwitchTo().getAlert().isAccept()) {
					a.accept();
				}
			} else {
				a = null;
			}
		}
		if (step.getSelect() != null) {
			org.xmlium.testsuite.Select stepSelect = step.getSelect();

			if (element != null && stepSelect.getSelectBy().getByIndex() != null) {
				Index index = stepSelect.getSelectBy().getByIndex();
				SelectData selectData = new SelectData();
				initSelectIndexes(element, index, selectData);
				if (selectData.selectIndex >= 0) {
					Select select = new Select(element);
					List<WebElement> options = select.getOptions();
					WebElement option = options.get(selectData.selectIndex);
					option.click();
				} else {
					throw new RuntimeException("selectedIndex=" + selectData.selectIndex);
				}
			} else {
				if (element == null) {
					element = findElement(step.getSelect().getFinds());
				}
				if (element != null) {
					Select select = new Select(element);
					if (step.getSelect().getSelectBy() != null) {
						selectBy(select, step.getSelect().getSelectBy());
					}
				}
			}
		}
		if (step.getPrettySelect() != null) {
			PrettySelect stepSelect = step.getPrettySelect();
			if (element == null) {
				if (stepSelect.getFinds() != null) {
					element = findElement(stepSelect.getFinds());
				}
			}
			if (element != null && stepSelect.getSelectBy() != null) {
				SelectData selectData = new SelectData();
				Select select = null;
				select = new Select(element);
				Index index = null;
				if (stepSelect.getSelectBy().getByIndex() != null) {
					index = stepSelect.getSelectBy().getByIndex();
					initSelectIndexes(element, index, selectData);
				} else if (stepSelect.getSelectBy().getByVisibleText() != null) {
					List<WebElement> elems = select.getOptions();
					getOptionIndexBuVisibleText(stepSelect.getSelectBy().getByVisibleText(), elems, selectData);
				}

				if (selectData.selectIndex >= 0) {
					WebElement arrowArea = null;
					WebElement scrollArea = null;
					if (stepSelect.getSelectBy().getArrowArea() != null) {
						arrowArea = findElement(stepSelect.getSelectBy().getArrowArea());
					} else {
						throw new RuntimeException("arrowArea not defined in xml");
					}

					if (arrowArea != null) {
						arrowArea.click();

						if (stepSelect.getSelectBy().getScrollArea() != null) {
							Finds scrollAreaFinds = stepSelect.getSelectBy().getScrollArea();
							if (scrollAreaFinds.getFind() != null) {
								scrollArea = findElement(scrollAreaFinds.getFind());
							} else if (scrollAreaFinds.getWaitFor() != null) {
								scrollArea = waitFor(scrollAreaFinds.getWaitFor());
							}
							if (scrollArea == null) {
								if (scrollAreaFinds.getFind() != null || scrollAreaFinds.getWaitFor() != null) {
									String text = "";
									if (scrollAreaFinds.getFind() != null
											&& scrollAreaFinds.getFind().getByXPath() != null) {
										text = unformatValue(scrollAreaFinds.getFind().getByXPath().getValue());
									} else if (scrollAreaFinds.getWaitFor() != null
											&& scrollAreaFinds.getWaitFor().getByXPath() != null) {
										text = unformatValue(scrollAreaFinds.getWaitFor().getByXPath().getValue());
									}

									throw new RuntimeException("scrollArea not found: " + text);
								} else {
									throw new RuntimeException("scrollArea not defined in xml");
								}
							}
						}

						if (index.isFirst() != null || index.isLast() != null) {
							Actions actions = new Actions(getSuite().getDriver());
							String text = null;
							if (index.isFirst() != null && index.isFirst()) {
								actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).perform();
							} else if (index.isLast() != null && index.isLast()) {
								actions.keyDown(Keys.CONTROL).sendKeys(Keys.END).perform();
							}

							if (index.isFirst() != null && index.isFirst()) {
								text = select.getOptions().get(0).getText();
							} else if (index.isLast() != null && index.isLast()) {
								text = select.getOptions().get(select.getOptions().size() - 1).getText();
							}
							if (stepSelect.getSelectBy().getOptionsTag() != null) {
								Find f = new Find();
								ByXPath xPath = new ByXPath();
								if (text != null && !text.isEmpty()) {
									xPath.setValue("//" + stepSelect.getSelectBy().getOptionsTag()
											+ "[contains(@class, '" + stepSelect.getSelectBy().getOptionsClasses()
											+ "') and text()='" + text + "']");
								} else {
									xPath.setValue("(//" + stepSelect.getSelectBy().getOptionsTag()
											+ "[contains(@class, '" + stepSelect.getSelectBy().getOptionsClasses()
											+ "')])[" + Integer.toString(selectData.selectIndex + 1) + "]");
								}
								f.setByXPath(xPath);
								WebElement optionElemnt = findElement(f);
								WebDriverWait wait = new WebDriverWait(getSuite().getDriver(), 30);
								wait.until(ExpectedConditions.elementToBeClickable(optionElemnt));
								optionElemnt.click();
							}
						} else {
							if (selectData.selectIndex < 0 || selectData.selectIndex >= select.getOptions().size()) {
								throw new RuntimeException("selectIndex: " + selectData.selectIndex + ", options.size: "
										+ select.getOptions().size());
							}
							WebElement option = select.getOptions().get(selectData.selectIndex);
							String text = option.getText();
							if (text == null || text.isEmpty()) {
								logger.debug("text is null or empty: " + option);
								text = null;
							}
							if (stepSelect.getSelectBy().getOptionsTag() != null) {
								Find f = new Find();
								ByXPath xPath = new ByXPath();
								if (text != null) {
									xPath.setValue("//" + stepSelect.getSelectBy().getOptionsTag()
											+ "[contains(@class, '" + stepSelect.getSelectBy().getOptionsClasses()
											+ "') and text()='" + text + "']");
								} else {
									xPath.setValue("(//" + stepSelect.getSelectBy().getOptionsTag()
											+ "[contains(@class, '" + stepSelect.getSelectBy().getOptionsClasses()
											+ "')])[" + Integer.toString(selectData.selectIndex + 1) + "]");
								}
								f.setByXPath(xPath);
								WebElement optionElemnt = findElement(f);

								Coordinates coordinate = ((Locatable) optionElemnt).getCoordinates();
								coordinate.onPage();
								coordinate.inViewPort();
								WebDriverWait wait = new WebDriverWait(getSuite().getDriver(), 30);
								wait.until(ExpectedConditions.elementToBeClickable(optionElemnt));
								optionElemnt.click();
							}
						}
					} else {
						throw new RuntimeException("can't click the arrow");
					}
				} else if (stepSelect.getSelectBy().getByVisibleText() != null) {
					WebElement arrowArea = null;
					WebElement scrollArea = null;
					if (stepSelect.getSelectBy().getArrowArea() != null) {
						arrowArea = findElement(stepSelect.getSelectBy().getArrowArea());
					} else {
						throw new RuntimeException("arrowArea not defined in xml");
					}

					if (arrowArea != null) {
						arrowArea.click();
						String scrollAreaXPath = null;
						if (stepSelect.getSelectBy().getScrollArea() != null) {
							Finds scrollAreaFinds = stepSelect.getSelectBy().getScrollArea();
							if (scrollAreaFinds.getFind() != null) {
								scrollArea = findElement(scrollAreaFinds.getFind());
							} else if (scrollAreaFinds.getWaitFor() != null) {
								scrollArea = waitFor(scrollAreaFinds.getWaitFor());
							}

							if (scrollAreaXPath == null) {
								if (scrollAreaFinds.getFind() != null || scrollAreaFinds.getWaitFor() != null) {
									if (scrollAreaFinds.getFind() != null
											&& scrollAreaFinds.getFind().getByXPath() != null) {
										scrollAreaXPath = unformatValue(scrollAreaFinds.getFind().getByXPath().getValue());
									} else if (scrollAreaFinds.getWaitFor() != null
											&& scrollAreaFinds.getWaitFor().getByXPath() != null) {
										scrollAreaXPath = unformatValue(scrollAreaFinds.getWaitFor().getByXPath().getValue());
									}
									// throw new RuntimeException("scrollArea
									// not found: " + text);
								} else {
									throw new RuntimeException("scrollArea not defined in xml");
								}
							}
						}

						Actions actions = new Actions(getSuite().getDriver());
						String text = null;
						// actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).perform();
						WebDriverWait wait = new WebDriverWait(getSuite().getDriver(), 30);
						wait.until(ExpectedConditions.elementToBeClickable(scrollArea));
						actions.moveToElement(scrollArea).build().perform();

						text = stepSelect.getSelectBy().getByVisibleText();
						logger.debug("text: " + text);
						if (stepSelect.getSelectBy().getOptionsTag() != null) {
							Find f = new Find();
							ByXPath xPath = new ByXPath();
							if (text != null) {

								xPath.setValue(
										scrollAreaXPath + "/descendant::" + stepSelect.getSelectBy().getOptionsTag()
												+ "[contains(@class, '" + stepSelect.getSelectBy().getOptionsClasses()
												+ "') and text()='" + text + "']");
							} else {
								xPath.setValue("(//" + stepSelect.getSelectBy().getOptionsTag() + "[contains(@class, '"
										+ stepSelect.getSelectBy().getOptionsClasses() + "')])["
										+ Integer.toString(selectData.selectIndex + 1) + "]");
							}
							f.setByXPath(xPath);
							logger.debug("xPath: " + xPath);
							WebElement optionElemnt = findElement(f);

							Coordinates coordinate = ((Locatable) optionElemnt).getCoordinates();
							coordinate.onPage();
							coordinate.inViewPort();
							wait = new WebDriverWait(getSuite().getDriver(), 30);
							wait.until(ExpectedConditions.visibilityOf(optionElemnt));
							actions.moveToElement(optionElemnt).click().build().perform();
						}
					}
				} else {
					throw new RuntimeException("selectedIndex=" + selectData.selectIndex);
				}
			} else {
				if (element == null) {
					element = findElement(step.getPrettySelect().getFinds());
				}
				if (element != null) {
					Select select = new Select(element);
					if (step.getPrettySelect().getSelectBy() != null) {
						selectBy(select, step.getPrettySelect().getSelectBy());
					}
				}
			}
		}

		if (step.getSleepAfter() != null) {
			long sleep = step.getSleepAfter().longValue();
			try {
				Thread.sleep(sleep);
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}

	protected void getOptionIndexBuVisibleText(String visibleText, List<WebElement> elems, SelectData selectData) {
		if (elems == null) {
			throw new RuntimeException("elems=null");
		}
		int i = -1;
		selectData.selectIndex = i;
		selectData.selectLength = new Integer(elems.size());
		logger.debug("elems.size(): " + elems.size());
		logger.debug("visibleText: " + visibleText);
		for (WebElement elem : elems) {
			i++;
			logger.debug("elem.getText(): " + elem.getText());
			if (visibleText.equals(elem.getText())) {
				selectData.selectIndex = i;
				return;
			}
		}
	}

	protected WebElement findElement(Finds finds) {
		if (finds == null) {
			return null;
		}
		if (finds.getFind() != null) {
			return findElement(finds.getFind());
		} else if (finds.getWaitFor() != null) {
			return waitFor(finds.getWaitFor());
		} else {
			throw new RuntimeException("no elements defined for Finds");
		}
	}

	protected void initSelectIndexes(WebElement element, Index index, SelectData selectData) {
		Select select = new Select(element);
		WebElement option = select.getFirstSelectedOption();
		List<WebElement> options = select.getOptions();
		if (selectData.selectLength == -1) {
			selectData.selectLength = new Integer(select.getOptions().size());
			selectData.selectIndex = options.indexOf(option);
		}
		if (index.isNext() != null && index.isNext()) {
			selectData.selectIndex++;
			if (selectData.selectIndex >= selectData.selectLength) {
				selectData.selectIndex = 0;
			}
		}
		if (index.isPrevious() != null && index.isPrevious()) {
			selectData.selectIndex--;
			if (selectData.selectIndex < 0) {
				selectData.selectIndex = selectData.selectLength - 1;
			}
		}
		if (index.isFirst() != null && index.isFirst()) {
			selectData.selectIndex = 0;
		}
		if (index.isLast() != null && index.isLast()) {
			selectData.selectIndex = options.size() - 1;
		}
		if (index.getMiddle() != null) {
			Middle middle = index.getMiddle();
			int remainder = selectData.selectLength % 2;
			selectData.selectIndex = selectData.selectLength / 2;
			if (remainder > 0 && middle.getRound() != null && middle.getRound().isUp() != null
					&& middle.getRound().isUp()) {
				selectData.selectIndex++;
			}
		}
	}

	protected void checkStore(Element e) {
		if (e.getStore() != null && e.getStore().getKey() != null) {
			store.put(e.getStore().getKey(), e);
		}
	}

	protected WebElement checkWaitFor(Element e) {
		if (e.getFinds() != null) {
			return findElement(e.getFinds());
		} else {
			throw new RuntimeException("Finds are missing in xml");
		}
	}

	protected void checkText(Element e, WebElement element) {
		if (e.getCheckTextEquals() != null) {
			CheckEqualsToString equals = e.getCheckTextEquals();
			if (!element.getText().contains(equals.getValue())) {
				throw new RuntimeException("no such element text");
			}
		}
	}

	protected void checkSendKeys(Element e, WebElement element) {
		if (e.getSendKeys() != null) {
			String value = checkValue(unformatValue(e.getSendKeys().getValue()));
			if (e.isCheckNullElement()) {
				if (element != null) {
					logger.debug(e.getSendKeys());
					//Actions actions = new Actions(getSuite().getDriver());

					if (e.getSendKeys().isRemoveOld()) {
						element.clear();
					}

					element.sendKeys(value);

					if (e.getSendKeys().isPressEnter()) {
						element.sendKeys(Keys.ENTER);
					}
				}
			} else {
				if (element != null) {
					logger.debug(e.getSendKeys());
					//Actions actions = new Actions(getSuite().getDriver());

					if (e.getSendKeys().isRemoveOld()) {
						element.clear();
					}

					element.sendKeys(value);

					if (e.getSendKeys().isPressEnter()) {
						element.sendKeys(Keys.ENTER);
					}
				}
			}
		}
	}

	protected void checkClick(Element e, WebElement element) throws Exception {
		if (e.isClick() != null && e.isClick().booleanValue()) {
			if (e.isCheckNullElement()) {
				if (element != null) {
					WebDriverWait wait = new WebDriverWait(getSuite().getDriver(), 30);
					wait.until(ExpectedConditions.elementToBeClickable(element));
					element.click();
				}
			} else {
				try {
					element = findElement(e.getFinds());
					WebDriverWait wait = new WebDriverWait(getSuite().getDriver(), 30);
					wait.until(ExpectedConditions.elementToBeClickable(element));
					element.click();
				} catch (Exception e1) {
					logger.debug("element: " + element);
					logger.debug("element: " + e);
					throw e1;
				}
			}
		}
	}

	protected void checkChangeState(Element e) {
		if (e.getChangeState() != null) {
			if (e.getFinds() != null) {
				findElement(e.getFinds());
			}
		}
	}

	protected Object switchTo(SwitchTo to) {
		if (to.getAlert() != null) {
			WebDriverWait wait = new WebDriverWait(getSuite().getDriver(), 2);
			Object o = wait.until(ExpectedConditions.alertIsPresent());
			return o;
		}
		if (to.getFrame() != null) {
			WebDriverWait wait = new WebDriverWait(getSuite().getDriver(), 10);
			Object o = wait
					.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name(unformatValue(to.getFrame().getValue()))));
			return o;
		}

		return null;
	}

	protected WebElement findElement(Find f) {
		WebElement element = null;
		By by = getBy(f);
		if (f.isCatchException()) {
			try {
				element = getSuite().getDriver().findElement(by);
			} catch (NoSuchElementException e) {
				System.err.println(e.getMessage());
			}
		} else {
			element = getSuite().getDriver().findElement(by);
		}
		return element;
	}

	protected By getByXPath(ByXPath xpath) {
		if (xpath.getValue() != null) {
			return By.xpath(unformatValue(xpath.getValue()));
		} else if (xpath.getIndexedValue() != null) {
			return getByIndexedValue(xpath, unformatValue(xpath.getIndexedValue()));
		} else {
			throw new IllegalArgumentException("ByXpath value missing");
		}
	}

	protected By getByIndexedValue(ByXPath ByXPath, String xpath) {
		Matcher xpathMatcher = xpathIndexStringPattern.matcher(xpath);
		if (xpathMatcher.matches()) {
			String value = xpathMatcher.group(2);
			if (value.equalsIgnoreCase("first")) {
				xpathIndex = new Integer(0);
			} else if (value.equalsIgnoreCase("next")) {
				if (xpathIndex != null && xpathIndex >= 0) {
					xpathIndex++;
				}
			} else if (value.equalsIgnoreCase("previous")) {
				if (xpathIndex != null && xpathIndex >= 0) {
					xpathIndex--;
				}
				if (xpathIndex < 0) {
					xpathIndex = (int) Byte.MAX_VALUE;
				}
			}
			String pattern = xpathMatcher.group(1);
			logger.debug(pattern);
			pattern = pattern.replaceFirst("\\$", Matcher.quoteReplacement("\\$"))
					.replaceFirst("\\{", Matcher.quoteReplacement("\\{"))
					.replaceFirst("\\}", Matcher.quoteReplacement("\\}"));
			logger.debug(pattern);
			xpath = xpath.replaceAll(pattern, xpathIndex.toString());
			logger.debug("xpath=" + xpath);
			ByXPath.setIndexedValue(null);
			ByXPath.setValue(xpath);
			return By.xpath(xpath);
		} else {
			return By.xpath(xpath);
		}
	}

	protected WebElement waitFor(final WaitFor wFor) {
		final By by = getBy(wFor);
		if (by == null) {
			throw new RuntimeException("By is null");
		}
		WebElement element = null;
		if (wFor.isCatchException()) {
			try {
				element = getSuite().getFluentWait().until(new Function<WebDriver, WebElement>() {

					public WebElement apply(WebDriver driverIn) {
						return driverIn.findElement(by);
					}
				});
			} catch (Exception e) {
				return null;
			}
		} else {
			element = getSuite().getFluentWait().until(new Function<WebDriver, WebElement>() {

				public WebElement apply(WebDriver driverIn) {
					return driverIn.findElement(by);
				}

			});
		}

		return element;
	}

	protected By getBy(WaitFor wFor) {
		if (wFor.getById() != null) {
			logger.debug(wFor.getById().getValue());
			return By.id(wFor.getById().getValue());
		}
		if (wFor.getByLinkText() != null) {
			String value = checkValue(unformatValue(wFor.getByLinkText().getValue()));
			return By.linkText(value);
		}
		if (wFor.getByName() != null) {
			return By.name(unformatValue(wFor.getByName().getValue()));
		}
		if (wFor.getByXPath() != null) {
			return getByXPath(wFor.getByXPath());
		} else if (wFor.getByCssSelector() != null) {
			return getBy(wFor.getByCssSelector());
		} else {
			throw new IllegalArgumentException("unknown: " + wFor);
		}
	}

	protected By getBy(Finds finds) {
		if (finds == null) {
			throw new RuntimeException("findds is null");
		}
		if (finds.getWaitFor() != null) {
			return getBy(finds.getWaitFor());
		} else if (finds.getFind() != null) {
			return getBy(finds.getFind());
		} else {
			throw new RuntimeException("waitFor is null: finds: " + finds);
		}
	}

	protected By getBy(ByCSSSelector cssSelector) {
		return By.cssSelector(unformatValue(cssSelector.getValue()));
	}

	protected By getBy(Find f) {
		if (f.getById() != null) {
			logger.debug(f.getById().getValue());
			return By.id(unformatValue(f.getById().getValue()));
		}
		if (f.getByLinkText() != null) {
			String value = checkValue(unformatValue(f.getByLinkText().getValue()));
			return By.linkText(value);
		}
		if (f.getByName() != null) {
			return By.name(unformatValue(f.getByName().getValue()));
		}
		if (f.getByXPath() != null) {
			return getByXPath(f.getByXPath());
		} else {
			throw new IllegalArgumentException("unknown: " + f);
		}
	}

	protected String checkValue(String value) {
		Matcher propertiesMatcher = propertiesStringPattern.matcher(value);
		Matcher expressionMatcher = expressionStringPattern.matcher(value);
		if (propertiesMatcher.matches()) {
			return getSuite().getBundle().getString(propertiesMatcher.group(1));
		} else if (expressionMatcher.matches()) {
			return evaluateExpression(expressionMatcher.group(1));
		} else {
			return value;
		}
	}

	protected String evaluateExpression(String expression) {
		int index = expression.indexOf("(");
		int index2 = expression.indexOf(")");
		index2++;
		if (index2 >= expression.length()) {
			index2 = -1;
		}
		String trail = null;
		String operation = null;
		String operand = null;
		if (index2 >= 0) {
			trail = expression.substring(index2);
		}
		if (trail != null) {
			switch (trail.charAt(0)) {
			case '+':
				operation = "+";
				operand = trail.substring(1);
				break;
			case '-':
				operation = "-";
				operand = trail.substring(1);
				break;
			case '*':
				operation = "*";
				operand = trail.substring(1);
				break;
			case '/':
				operation = "/";
				operand = trail.substring(1);
				break;
			}
		}
		String theClassWithMethod = expression.substring(0, index);
		int index3 = theClassWithMethod.lastIndexOf('.');
		String theClass = theClassWithMethod;
		String theMethod = null;
		if (index3 > -1) {
			theClass = theClassWithMethod.substring(0, index3);
			theMethod = theClassWithMethod.substring(index3 + 1, theClassWithMethod.length());
		}
		logger.debug("theClassWithMethod: " + theClassWithMethod);
		logger.debug("theClass: " + theClass);
		logger.debug("theMethod: " + theMethod);
		try {
			Class<?> c = Class.forName(theClass);

			Method method = null;
			if (theMethod != null && theMethod.length() > 0) {
				Method[] methods = c.getDeclaredMethods();
				for (int i = 0; i < methods.length; i++) {
					if (methods[i].getName().equals(theMethod)) {
						method = methods[i];
						break;
					}
				}
				if (method != null) {
					Object ret = null;
					// if(classObject!=null){
					if (Modifier.isStatic(method.getModifiers())) {
						ret = method.invoke(null);
					} else {
						Object classObject = c.newInstance();
						ret = method.invoke(classObject);
					}
					if (ret instanceof Integer || ret instanceof Double || ret instanceof Float) {
						if (operation != null && operand != null) {
							switch (operation.charAt(0)) {
							case '*':
								return multiply(ret, operand);
							default:
								throw new UnsupportedOperationException(operation);
								// break;
							}
						}
					}
					/*
					 * }else{
					 * logger.warn("Failed to create instance of "+c.getName());
					 * }
					 */
				} else {
					logger.warn("method: " + theMethod + " doesn't exists in class: " + c.getName());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	public String multiply(Object ret, String operand) {
		Integer intOper = null;
		Double dblOper = null;
		Float fltOper = null;
		try {
			intOper = Integer.parseInt(operand);
		} catch (Exception e) {
		}
		try {
			dblOper = Double.parseDouble(operand);
		} catch (Exception e) {
		}
		try {
			fltOper = Float.parseFloat(operand);
		} catch (Exception e) {
		}
		if (ret instanceof Integer) {
			Integer intRet = (Integer) ret;
			if (intOper != null) {
				return String.valueOf(intRet * intOper);
			}
			if (dblOper != null) {
				return String.valueOf((int) (intRet * dblOper));
			}
			if (fltOper != null) {
				return String.valueOf((int) (intRet * fltOper));
			}
		}
		if (ret instanceof Double) {
			Double doubleRet = (Double) ret;
			if (intOper != null) {
				return String.valueOf((int) (doubleRet * intOper));
			}
			if (dblOper != null) {
				return String.valueOf((int) (doubleRet * dblOper));
			}
			if (fltOper != null) {
				return String.valueOf((int) (doubleRet * fltOper));
			}
		}
		return null;
	}

	protected void selectBy(Select select, Object objectSelectBy) {
		if (objectSelectBy instanceof SelectBy) {
			SelectBy selectBy = (SelectBy) objectSelectBy;
			if (selectBy.getByVisibleText() != null) {
				select.selectByVisibleText(selectBy.getByVisibleText());
			}
		} else if (objectSelectBy instanceof PrettySelectBy) {
			PrettySelectBy selectBy = (PrettySelectBy) objectSelectBy;
			select.deselectByValue(selectBy.getByVisibleText());
		} else {
			throw new RuntimeException("unknow SelectBy: " + objectSelectBy.getClass().getName());
		}
	}

	List<String> getAvailableValuesForSteps() {
		List<String> retValues = new ArrayList<String>();
		for (StepType step : steps) {
			if (step != null) {
				retValues.addAll(getElementValues(step.getElement()));
				retValues.addAll(getPrettySelectValues(step.getPrettySelect()));
				retValues.addAll(getSelectValues(step.getSelect()));
				retValues.addAll(getSwitchToValues(step.getSwitchTo()));
			}
		}
		return retValues;
	}

	protected List<String> getFindsValues(Finds finds) {
		List<String> retValues = new ArrayList<String>();

		if (finds == null) {
			return retValues;
		}

		Find f = finds.getFind();

		if (f != null) {
			if (f.getById() != null && f.getById().getValue() != null && !f.getById().getValue().isEmpty()) {
				retValues.add(unformatValue(f.getById().getValue()));
			}
			if (f.getByLinkText() != null && f.getByLinkText().getValue() != null
					&& !f.getByLinkText().getValue().isEmpty()) {
				retValues.add(unformatValue(f.getByLinkText().getValue()));
			}

			if (f.getByName() != null && f.getByName().getValue() != null && !f.getByName().getValue().isEmpty()) {
				retValues.add(unformatValue(f.getByName().getValue()));
			}

			if (f.getByXPath() != null && f.getByXPath().getValue() != null && !f.getByXPath().getValue().isEmpty()) {
				retValues.add(unformatValue(f.getByXPath().getValue()));
			}
		}

		WaitFor waitFor = finds.getWaitFor();
		if (waitFor != null) {
			if (waitFor.getById() != null && waitFor.getById().getValue() != null
					&& !waitFor.getById().getValue().isEmpty()) {
				retValues.add(unformatValue(waitFor.getById().getValue()));
			}
			if (waitFor.getByLinkText() != null && waitFor.getByLinkText().getValue() != null
					&& !waitFor.getByLinkText().getValue().isEmpty()) {
				retValues.add(unformatValue(waitFor.getByLinkText().getValue()));
			}

			if (waitFor.getByName() != null && waitFor.getByName().getValue() != null
					&& !waitFor.getByName().getValue().isEmpty()) {
				retValues.add(unformatValue(waitFor.getByName().getValue()));
			}

			if (waitFor.getByXPath() != null && waitFor.getByXPath().getValue() != null
					&& !waitFor.getByXPath().getValue().isEmpty()) {
				retValues.add(unformatValue(waitFor.getByXPath().getValue()));
			}
		}
		return retValues;
	}

	protected List<String> getElementValues(Element elem) {
		List<String> retValues = new ArrayList<String>();
		if (elem == null) {
			return retValues;
		}

		retValues.addAll(getFindsValues(elem.getFinds()));

		return retValues;
	}

	protected List<String> getPrettySelectValues(PrettySelect elem) {
		List<String> retValues = new ArrayList<String>();

		if (elem == null) {
			return retValues;
		}

		retValues.addAll(getFindsValues(elem.getFinds()));
		PrettySelectBy selectBy = elem.getSelectBy();
		if (selectBy != null) {
			retValues.add(unformatValue(selectBy.getByVisibleText()));
			retValues.add(unformatValue(selectBy.getOptionsClasses()));
			retValues.addAll(getFindsValues(selectBy.getArrowArea()));
			retValues.addAll(getFindsValues(selectBy.getScrollArea()));
		}

		return retValues;
	}

	protected List<String> getSelectValues(org.xmlium.testsuite.Select elem) {
		List<String> retValues = new ArrayList<String>();

		if (elem == null) {
			return retValues;
		}

		retValues.addAll(getFindsValues(elem.getFinds()));
		SelectBy selectBy = elem.getSelectBy();
		if (selectBy != null) {
			retValues.add(unformatValue(selectBy.getByVisibleText()));
		}

		return retValues;
	}

	protected List<String> getSwitchToValues(SwitchTo elem) {
		List<String> retValues = new ArrayList<String>();

		if (elem == null) {
			return retValues;
		}

		return retValues;
	}

	protected void checkStoreValue(Element e, WebElement element) {
		logger.debug(e);
		if (e.getStoreValue() != null && e.getStoreValue().getKey() != null) {

			String strValue = element.getAttribute("value");
			if (strValue == null || strValue.isEmpty()) {
				if (!element.getTagName().equalsIgnoreCase("input")) {
					strValue = element.getText();
				}
			}
			if (strValue == null || strValue.isEmpty()) {
				throw new RuntimeException(e.toString() + "\n value attribute is empty or null!");
			}
			elementValuesMap.put(unformatValue(e.getStoreValue().getKey()), strValue);
		} else if (e.getStoreValue() != null && e.getStoreValue().getKey() == null) {
			throw new RuntimeException(e.toString() + "\n key==null!");
		}
	}

	protected void checkSetValue(Element e, WebElement element) throws Exception {
		if (e.getSetValue() != null && e.getSetValue().getKeys() != null) {
			BigDecimal operand1 = null;
			BigDecimal operand2 = null;
			String operation = null;

			Values values = e.getSetValue().getKeys();
			List<String> keys = values.getKey();
			if (keys == null) {
				throw new RuntimeException(e.toString() + "\n no values " + e + "!");
			}
			String strValueExpression = unformatValue(e.getSetValue().getValueExpression());
			if (strValueExpression == null || strValueExpression.isEmpty()) {
				throw new RuntimeException(e.toString() + " valueExpressionis null or empty!");
			}
			String oldValue1 = null;
			String oldValue2 = null;
			if (keys.size() == 2) {
				oldValue1 = elementValuesMap.get(unformatValue(values.getKey().get(0)));
				oldValue2 = elementValuesMap.get(unformatValue(values.getKey().get(1)));
				if (oldValue1 == null) {
					throw new RuntimeException(e.toString() + "\n no value for key=" + values.getKey().get(0) + "!");
				}
				if (oldValue2 == null) {
					throw new RuntimeException(e.toString() + "\n no value for key=" + values.getKey().get(1) + "!");
				}
			} else if (keys.size() == 1) {
				oldValue1 = elementValuesMap.get(unformatValue(values.getKey().get(0)));
				if (oldValue1 == null) {
					throw new RuntimeException(e.toString() + "\n no value for key=" + unformatValue(values.getKey().get(0)) + "!");
				}
			}
			if (oldValue1 != null && oldValue2 != null) {
				Matcher valueMatcher = valueExpressionStringPattern3.matcher(strValueExpression);

				if (valueMatcher.matches()) {
					String value1 = valueMatcher.group(1);
					// String value2 = valueMatcher.group(3);
					if (value1.equals("${0}")) {
						try {
							operand1 = (BigDecimal) getSuite().getCurrencyFormat().parse(oldValue1);
							operand2 = (BigDecimal) getSuite().getCurrencyFormat().parse(oldValue2);
						} catch (ParseException e2) {
							logger.error(e2.getMessage(), e2);
						}
					} else {
						try {
							operand2 = (BigDecimal) getSuite().getCurrencyFormat().parse(oldValue1);
							operand1 = (BigDecimal) getSuite().getCurrencyFormat().parse(oldValue2);
						} catch (ParseException e2) {
							logger.error(e2.getMessage(), e2);
						}
					}
					operation = valueMatcher.group(2);
					logger.debug("oldValue1=" + oldValue1);
					logger.debug("oldValue2=" + oldValue2);
					logger.debug("operand1=" + operand1);
					logger.debug("operand2=" + operand2);
					logger.debug("operation=" + operation);
				} else {
					throw new UnsupportedOperationException("not supported yet!");
				}
			} else {
				Matcher valueMatcher1 = valueExpressionStringPattern1.matcher(strValueExpression);
				Matcher valueMatcher2 = valueExpressionStringPattern2.matcher(strValueExpression);

				if (valueMatcher1.matches()) {
					String newValue = valueMatcher1.group(1);
					operation = valueMatcher1.group(2);
					logger.debug("newValue=" + newValue);
					logger.debug("operation=" + operation);
					try {
						operand1 = (BigDecimal) getSuite().getCurrencyFormat().parse(newValue);
						operand2 = (BigDecimal) getSuite().getCurrencyFormat().parse(oldValue1);
					} catch (ParseException e2) {
						logger.error(e2.getMessage(), e2);
					}
				} else if (valueMatcher2.matches()) {
					String newValue = valueMatcher2.group(3);
					operation = valueMatcher2.group(2);
					logger.debug("newValue=" + newValue);
					logger.debug("operation=" + operation);
					try {
						operand2 = (BigDecimal) getSuite().getCurrencyFormat().parse(newValue);
						operand1 = (BigDecimal) getSuite().getCurrencyFormat().parse(oldValue1);
					} catch (ParseException e2) {
						logger.error(e2.getMessage(), e2);
					}
				} else {
					throw new UnsupportedOperationException("not supported yet!");
				}
			}

			if (operand1 != null && operand2 != null && operation != null && !operation.isEmpty()) {
				BigDecimal value = computeValue(operation, operand1, operand2);

				try {
					element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
					element.sendKeys( getSuite().getCurrencyFormat().format(value), Keys.ENTER);

				} catch (Exception e1) {
					logger.error(e, e1);
					throw e1;
				}
			} else {
				throw new RuntimeException(
						e.toString() + "operand1=" + operand1 + ", operand2=" + operand2 + ", operation=" + operation);
			}

		} else if (e.getSetValue() != null && e.getSetValue().getKeys() == null) {
			throw new RuntimeException(e.toString() + "\n key==null!");
		}
	}

	protected BigDecimal computeValue(String operation, BigDecimal operand1, BigDecimal operand2) {
		char op = operation.charAt(0);
		BigDecimal ret = null;
		switch (op) {
		case '+':
			ret = operand1.add(operand2);
			break;

		case '-':
			ret = operand1.subtract(operand2);
			break;

		case '*':
			ret = operand1.multiply(operand2);
			break;
		case '/':
			ret = operand1.divide(operand2, BigDecimal.ROUND_HALF_UP);
			break;

		default:
			break;
		}
		if (ret != null) {
			ret = ret.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return ret;
	}

	protected void checkMoveX(Element e, WebElement element) {
		if (e.getMoveX() != null && e.getElement() != null) {
			int len = e.getMoveX().intValue();
			Keys key = Keys.ARROW_RIGHT;
			if (e.getMoveX().intValue() < 0) {
				len = -e.getMoveX().intValue();
				key = Keys.ARROW_LEFT;
			}
			logger.debug("len="+len);
			for (int i = 0; i < len; i++) {
				logger.debug("in loop: i="+i);
				try {
					Thread.sleep(120);
				} catch (Exception e2) {
					// TODO: handle exception
				}
				element = findElement(e.getFinds());
				element.sendKeys(key);
				
			}
		}
	}

	public XMLTestSuite getSuite() {
		return suite;
	}
	
	public static String unformatValue(String v){
		String s = unformatValueRemoveFormat(v);
		s = unformatValueRemoveSpaces(s);
		return s;
	}

	protected static final Pattern removeTabsAndNewLinesPattern  = Pattern.compile("([.&&^\n&&^\t]*)(\n\t*)(\t*)(.*)");

	public static String unformatValueRemoveFormat(String v){
		Matcher m = null;
		logger.debug("value before: '"+v.replaceAll("\n", "\\n").replaceAll("\t", "\\t").replaceAll("\f", "\\f")+"'");
		String s = v;
		m =  removeTabsAndNewLinesPattern.matcher(s);
		if(m.matches()){
			String a = m.group(1);
			String b = m.group(4);
			String c = unformatValueRemoveFormat(b);
			if(!c.isEmpty()){
				s = a+" "+c;
			}else{
				s = a;
			}
		}else{
			logger.debug("doesn't match!");
		}
		logger.debug("value in loop: '"+s.replaceAll("\n", "\\n").replaceAll("\t", "\\t").replaceAll("\f", "\\f")+"'");

		s = s.replaceAll("\t", "");
		s = s.replaceAll("\n", " ");
		while (s.endsWith(" ")) {
			s = s.substring(0, s.length()-1);
		}
		logger.debug("value: '"+s+"'");
		return s;
	}

	
	protected static final Pattern removeSpacesPattern  = Pattern.compile("(.*)(')( *)([\\w \\d]*)( *)(')(.*)");

	public static String unformatValueRemoveSpaces(String v){
		logger.debug("value before: \""+v+"\"");
		Matcher m =  removeSpacesPattern.matcher(v);
		String s = v;
		if(m.matches()){
			String a = m.group(1);
			logger.debug("a: \""+a+"\"");
			String b = m.group(2);
			logger.debug("b: \""+b+"\"");
			String c = m.group(4);
			logger.debug("c: \""+c+"\"");
			String d = m.group(6);
			logger.debug("d: \""+d+"\"");
			String e = m.group(7);
			logger.debug("e: \""+e+"\"");
			e = unformatValueRemoveSpaces(e);
			logger.debug("e2: \""+e+"\"");
			if(!e.isEmpty()){
				s = a+b+c+d+e;
			}else{
				s = a+b+c+d;
			}
		}else{
			logger.debug("doesn't match!");
		}
		logger.debug("value: \""+s+"\"");
		return s;
	}
}
