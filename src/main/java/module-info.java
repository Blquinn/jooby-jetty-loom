/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
import io.jooby.Server;
import com.github.blquinn.jooby.jettyloom.JettyServer;

module io.jooby.jetty {
  exports com.github.blquinn.jooby.jettyloom;

  requires io.jooby;
  requires com.github.spotbugs.annotations;
  requires typesafe.config;
  requires org.eclipse.jetty.server;
  requires org.eclipse.jetty.servlet;
  requires org.eclipse.jetty.websocket.jetty.server;
  requires org.eclipse.jetty.alpn.server;
  requires org.eclipse.jetty.http2.server;

  provides Server with
      JettyServer;
}
