package com.morlunk.mumbleclient;

public interface SmsListener {
  void onMessageReceived(String messageText);
}