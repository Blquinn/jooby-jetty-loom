/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package com.github.blquinn.jooby.internal.jettyloom;

import java.io.IOException;

import org.eclipse.jetty.server.HttpOutput;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jooby.Sender;

public class JettySender implements Sender {
  private final JettyContext ctx;
  private final HttpOutput sender;

  public JettySender(JettyContext ctx, HttpOutput sender) {
    this.ctx = ctx;
    this.sender = sender;
  }

  @Override
  public Sender write(@NonNull byte[] data, @NonNull Callback callback) {
    try {
      sender.write(data);
      sender.flush();
      callback.onComplete(ctx, null);
    } catch (IOException e) {
      callback.onComplete(ctx, e);
    }
    return this;
  }

  @Override
  public void close() {
    ctx.complete(null);
  }
}
