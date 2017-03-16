package com.sitech.crmpd.idmm.ble.mon;


import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.sitech.crmpd.idmm.ble.RunTime;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Values;

/**
 * Created by guanyf on 2016/12/12.
 */
public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter{

    private ByteBufToBytes reader;
    private String uri;
    private RunTime rt;
    private HttpRequest request;

    public HttpServerInboundHandler(RunTime rt) {
        this.rt = rt;
    }

    /**
     * 这个函数在一次http请求过程中会被调用2次， 一次是http header， 第二次是body
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("call me once:" + this);
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            //System.out.println("messageType:" + request.headers().get("messageType"));
            //System.out.println("businessType:" + request.headers().get("businessType"));
            if (HttpHeaders.isContentLengthSet(request)) {
                reader = new ByteBufToBytes((int) HttpHeaders.getContentLength(request));
            }
            uri = request.getUri();
        }

        if (msg instanceof HttpContent) {
            String body = null;
            if(reader == null){
                // GET, without body
            }else {
                // POST, read body first
                HttpContent httpContent = (HttpContent) msg;
                ByteBuf content = httpContent.content();
                reader.reading(content);
                content.release();

                if (reader.isEnd()) {
                    body = new String(reader.readFull());
                    System.out.println("Client post:" + body);

                }else{
                    return;
                }
            }
            String output = "I am ok";
            System.out.println(uri);
            if("/info".equals(uri)){
                if(rt != null){
                    output = rt.info();
                }
            }else if(uri.startsWith("/lockdetail/")){
                String[] x = uri.split("/");
                if(x.length < 4){
                    output = "invalid parameter!";
                }else{
                    if(rt != null){
                        //client_id, dest_topic_id
                        output = rt.lockdetail(x[2], x[3]);
                    }
                }
            }


            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                    Unpooled.wrappedBuffer(output.getBytes())
            );
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            ctx.write(response);
            ctx.flush();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //logger.info("HttpServerInboundHandler.channelReadComplete");
        ctx.flush();
    }
}
