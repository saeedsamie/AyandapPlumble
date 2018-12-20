package com.morlunk.ayandap;

public interface SmsListener {
  void onMessageReceived(String messageText);
}