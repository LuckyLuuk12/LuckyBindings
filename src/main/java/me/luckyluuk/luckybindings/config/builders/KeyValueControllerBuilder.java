package me.luckyluuk.luckybindings.config.builders;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import me.luckyluuk.luckybindings.model.Tuple;

public interface KeyValueControllerBuilder extends ControllerBuilder<Tuple<String, ?>> {
  static KeyValueControllerBuilder create(Option<Tuple<String, ?>> option) {
    return new KeyValueControllerBuilderImpl(option);
  }
}
