package me.luckyluuk.luckybindings.config.builders;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import me.luckyluuk.luckybindings.config.controllers.KeyValueController;
import me.luckyluuk.luckybindings.model.Tuple;

public class KeyValueControllerBuilderImpl extends AbstractControllerBuilderImpl<Tuple<String, ?>> implements KeyValueControllerBuilder {
  public KeyValueControllerBuilderImpl(Option<Tuple<String, ?>> option) {
    super(option);
  }

  @Override
  public Controller<Tuple<String, ?>> build() {
    return new KeyValueController(option);
  }
}
