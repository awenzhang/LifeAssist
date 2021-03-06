package testGroupId.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Reactor implements Runnable {
  private ServerSocketChannel serverSocketChannel = null;

  private Selector selector = null;

  public Reactor() {
    try {
      selector = Selector.open();
      serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.socket().bind(new InetSocketAddress(8888));
      SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      selectionKey.attach(new Acceptor());
      System.out.println("服务器启动正常!");
    } catch (IOException e) {
      System.out.println("启动服务器时出现异常!");
      e.printStackTrace();
    }
  }

  public void run() {
    while (true) {
      try {
        selector.select();

        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        while (iter.hasNext()) {
          SelectionKey selectionKey = iter.next();
          dispatch((Runnable) selectionKey.attachment());
          iter.remove();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void dispatch(Runnable runnable) {
    if (runnable != null) {
      runnable.run();
    }
  }

  public static void main(String[] args) {
    new Thread(new Reactor()).start();
  }

  class Acceptor implements Runnable {
    public void run() {
      try {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
          System.out.println(
              "接收到来自客户端（" + socketChannel.socket().getInetAddress().getHostAddress() + "）的连接");
          new Handler(selector, socketChannel);
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
