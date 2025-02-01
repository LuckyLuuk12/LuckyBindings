package me.luckyluuk.luckybindings.model;

import lombok.Data;

@Data
public class KeyBind {
  private final String key;
  private final String action;
  private final String[] args;
  private boolean enabled = true;
}
