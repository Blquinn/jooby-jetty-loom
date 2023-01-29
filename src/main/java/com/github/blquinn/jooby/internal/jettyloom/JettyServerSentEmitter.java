/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package com.github.blquinn.jooby.internal.jettyloom;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.util.thread.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jooby.Context;
import io.jooby.Server;
import io.jooby.ServerSentEmitter;
import io.jooby.ServerSentMessage;
import io.jooby.SneakyThrows;

public class JettyServerSentEmitter implements ServerSentEmitter {
  private Logger log = LoggerFactory.getLogger(ServerSentEmitter.class);

  private JettyContext jetty;

  private AtomicBoolean open = new AtomicBoolean(true);

  private String id;

  private SneakyThrows.Runnable closeTask;

  public JettyServerSentEmitter(JettyContext jetty) {
    this.jetty = jetty;
    this.id = UUID.randomUUID().toString();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public ServerSentEmitter setId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public boolean isOpen() {
    return open.get();
  }

  @NonNull @Override
  public Context getContext() {
    return Context.readOnly(jetty);
  }

  @NonNull @Override
  public ServerSentEmitter send(ServerSentMessage data) {
    if (isOpen()) {
      HttpOutput output = jetty.response.getHttpOutput();
      try {
        output.write(data.toByteArray(jetty));
        output.flush();
      } catch (Throwable x) {
        if (Server.connectionLost(x) || x instanceof EofException) {
          close();
        } else {
          log.error(
              "server-sent-event resulted in exception: id {} {}", id, jetty.getRequestPath(), x);
          if (SneakyThrows.isFatal(x)) {
            throw SneakyThrows.propagate(x);
          }
        }
      }
    }
    return this;
  }

  @Override
  public ServerSentEmitter keepAlive(long timeInMillis) {
    if (isOpen()) {
      Scheduler scheduler = jetty.request.getHttpChannel().getConnector().getScheduler();
      scheduler.schedule(new KeepAlive(this, timeInMillis), timeInMillis, TimeUnit.MILLISECONDS);
    }
    return this;
  }

  @Override
  public void onClose(SneakyThrows.Runnable task) {
    this.closeTask = task;
  }

  @NonNull @Override
  public void close() {
    if (open.compareAndSet(true, false)) {
      try {
        if (closeTask != null) {
          closeTask.run();
        }
      } finally {
        try {
          jetty.response.closeOutput();
        } catch (IOException x) {
          log.debug("server-sent event close resulted in exception", x);
        }
      }
    }
  }
}
