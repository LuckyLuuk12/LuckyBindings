package me.luckyluuk.luckybindings.config.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import me.luckyluuk.luckybindings.config.elements.TwoStringControllerElement;
import me.luckyluuk.luckybindings.model.Tuple;
import net.minecraft.text.Text;

// TODO: Figure out how to make proper custom controllers
public class KeyValueController implements IStringController<Tuple<String, ?>> {
  protected final Option<Tuple<String, ?>> option;

  public KeyValueController(Option<Tuple<String, ?>> option) {
    this.option = option;
  }
  /**
   * Returns the key of the option's pending value.
   *
   * @see Option#pendingValue()
   */
  @Override
  public String getString() {
    return option.pendingValue().getFirst();
  }
  /**
   * Returns the value of the option's pending value.
   * @return The value of the option's pending value.
   */
  public Object getTupleValue() {
    if(option.pendingValue().getSecond() == null) return null;
    return option.pendingValue().getSecond();
  }
  public String getString2() {
    if(option.pendingValue().getSecond() == null) return "";
    return option.pendingValue().getSecond().toString();
  }

  /**
   * Sets the option's pending value from a string.
   *
   * @param newKey The string to set the option's pending value to.
   * @see Option#requestSet(Object)
   */
  @Override
  public void setFromString(String newKey) {
    option.requestSet(new Tuple<>(newKey, option.pendingValue().getSecond()));
  }

  public void setFromString2(String newValue) {
    option.requestSet(new Tuple<>(option.pendingValue().getFirst(), newValue));
  }

  public void setFromString(String value, String secondValue) {
    option.requestSet(new Tuple<>(value, secondValue));
  }

  /**
   * Gets the dedicated {@link Option} for this controller
   */
  @Override
  public Option<Tuple<String, ?>> option() {
    return option;
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public Text formatValue() {
    return IStringController.super.formatValue();
  }
  public Text formatValue2() {
    return Text.of(getString2());
  }

  @Override
  public boolean isInputValid(String input) {
    return IStringController.super.isInputValid(input);
  }
  @Override
  public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
    return new TwoStringControllerElement(this, screen, widgetDimension, false);
  }
}