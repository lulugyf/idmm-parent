#-*- coding: gbk -*-

import urllib
from time import strftime,localtime
import time
import json
import os
import random
from kazoo import client
import traceback
import logging
import pycurl
from cStringIO import StringIO

'''
    start = strftime("%Y-%m-%d %H:%M:%S", localtime())
    
    c = client('20.26.20.87:59035')
    c.send('T109MarkDeal', 'Pub115', u'stop 13900')
    c.send_commit('T109MarkDeal', 'Pub115', 10, 13900)
    
    end = strftime("%Y-%m-%d %H:%M:%S", localtime())
    print 'begin time:%s,end time:%s' %(start, end)
'''

class DMMClient:
    def __init__(self, addr):
        self.c = pycurl.Curl()
        self.send_url = 'http://%s/SEND'%addr
        self.send_commit_url = 'http://%s/SEND_COMMIT'%addr
        self.fetch_url = 'http://%s/PULL'%addr
    
    def post(self, url, data, headers=None):
        c = self.c
        storage = StringIO()
        url=url.decode('utf-8').encode('iso8859-1')
        c.setopt(pycurl.URL, url)
        if headers is not None:
            c.setopt(pycurl.HTTPHEADER, headers)
        c.setopt(pycurl.POST, 1)
        c.setopt(pycurl.POSTFIELDS, json.dumps(data))
        c.setopt(c.WRITEFUNCTION, storage.write)
        c.perform()
        if storage.getvalue().find('rejected') != -1:
            j = storage.getvalue()
            storage.close()
            return j
        else:
            j = json.loads(storage.getvalue())
            storage.close()
            return j
    
    def send(self, topic, client, content, priority, group):
        data = {"topic":topic,"client-id":client,"content":content,"priority":priority,"group":group,"visit-password":123456}
        j = self.post(self.send_url, data, ['Content-Type: text/plain; charset=GBK'])
        if j.find('rejected') != -1:
            return j
        elif j['result-code'] != 'OK':
            print 'send failed'
            return
        print j['message-id'] , content
        return j['message-id']
        
    def send_commit(self, topic, client, messageid):
        data = {"topic":topic,"client-id":client,"message-id":messageid,"custom.commit-code":"true"}
        j = self.post(self.send_commit_url, data)
        if j['result-code'] != 'OK':
            print 'send failed'
            return
        print 'send commit return', j['result-code']
        
    def send_rollback(self, topic, client, messageid):
        data = {"topic":topic,"client-id":client,"message-id":messageid,"custom.commit-code":"false"}
        #j = self.post(self.send_commit_url, data)
        #if j['result-code'] != 'OK':
        #    print 'send failed', x
        #    return
        #print 'send rollback return', j['result-code']
    
    def fetch(self, topic, client, process_time=60):
        data = {'target-topic':topic,'client-id':client, 'processing-time':process_time}
        
        j = self.post(self.fetch_url, data)
        print j
        if isinstance(j, dict)==True:
            if j['result-code'] != 'OK':
                print 'fetch failed'
                return None, None
            else:
                msgid = j['message-id']
                content = j['content']
                return msgid, content
        elif isinstance(j, str)==True:
            return j, None
            
        
    def fetch_commit(self, topic, client, msgid, process_time=60):
        data = {'target-topic':topic,'client-id':client, 'message-id':msgid, 'pull-code':'COMMIT'}
        j = self.post(self.fetch_url, data)
        print 'commit return', j['result-code']

def getBrokerList(zkURL, node_dir = '/idmm2/httpbroker'):
    logging.basicConfig()
    brokerlist = []
    print '%s' %zkURL
    try:
        zk = client.KazooClient(hosts='%s' %zkURL)
        zk.start()
        addr_list = zk.get_children(node_dir)
        print 'addr_list=%s' %addr_list
        #if len(addr_list)>0:
        #    for id in addr_list:
        #        tmp_path = node_dir + '/' + id
        #        data,stat = zk.get('%s' %tmp_path)
        #        d = data.split("//")[1]
        #        addr = d.split("/")[0]
        #        brokerlist.append(addr)
        zk.stop()
        return addr_list
    except Exception, e:
        print traceback.format_exc()

def main():
    addr_list = getBrokerList("10.113.183.36:4621")
    c = DMMClient('%s' %(addr_list[0]))
    
    topic = 'T_mon'
    clientid = 'mon'
    while True:
        msgid, content = c.fetch(topic, clientid)
        if msgid==None and content==None:
            time.sleep(5.0)
            print 'NO MORE MESSAGE'
            continue
        if isinstance(msgid, str)==True and msgid.find('rejected') != -1:
            print msgid
            time.sleep(5.0)
        else:
            print 'msgid=%s,content=%s' %(msgid,content)
            c.fetch_commit(topic, clientid, msgid)

if __name__ == '__main__':
    main() 
    
