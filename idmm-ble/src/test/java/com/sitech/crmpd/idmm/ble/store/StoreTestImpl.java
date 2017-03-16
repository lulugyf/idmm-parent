package com.sitech.crmpd.idmm.ble.store;


import com.sitech.crmpd.idmm.ble.store.JournalOP;
import com.sitech.crmpd.idmm.ble.store.LoadCallback;
import com.sitech.crmpd.idmm.ble.store.PrioQueue;
import com.sitech.crmpd.idmm.ble.store.Store;

import java.io.IOException;

/**
 * Created by guanyf on 2016/4/27.
 */
public class StoreTestImpl implements Store {
    private String dst_topic_id;
    private String dst_cli_id;

    @Override
    public boolean put(JournalOP op) {
//        System.out.println("=====to store:"+op.op.name() + "   msgid:"+op.msgid);
        return true;
    }

    @Override
    public void setTopic(String topic_id) {
        this.dst_topic_id = topic_id;
    }

    @Override
    public void setClient(String client_id) {
        this.dst_cli_id = client_id;
    }

    @Override
    public String getTopic() {
        return dst_topic_id;
    }

    @Override
    public String getClient() {
        return dst_cli_id;
    }

    @Override
    public void restore(LoadCallback q) throws IOException {
        q.finishLoading(null);
    }

    @Override
    public void archive() throws IOException {

    }

    @Override
    public void archive(PrioQueue q) throws IOException {
        
    }

    @Override
    public void removeQueue() {

    }

    @Override
    public void close() throws IOException {

    }
}
