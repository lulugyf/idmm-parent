#coding=utf-8

from idmm2_client import getBrokerList, IDMMClient

''' 测试消费者通知
#-- 消费者通知配置
insert into consume_notice_info_9 values('TRecOprCntt', 'Pub101', 
 'TRecOprCnttDest', 'Sub119Opr', 'notice_1', 'notice_sub_1', '1',
 'aa', now(), 'note');
'''

def produce_and_consume(addr):
    #addr_list = getBrokerList("172.21.1.46:2181")
    #c = IDMMClient('10.162.200.221:42124')
    
    c = IDMMClient(addr)
    c.setPassword('pub_Test')

    # for guanyf local pc
    #pub_topic = 'TRecOprCntt'
    #pub_client = 'Pub101'
    #sub_topic = 'TRecOprCnttDest'
    #sub_client = 'Sub119Opr'

    # for 172.21.0.46
    pub_topic = 'Test'
    pub_client = 'pub_Test'
    sub_topic = 'Test'
    sub_client = 'sub_Test'
    
    msgid = c.send(pub_topic, pub_client, u'stop 13900')
    if msgid is None:
        return False
    print 'message id=%s' %msgid
    c.send_commit(pub_topic, pub_client, msgid, 10, 13900)

    # consume
    last_msgid = None
    pull_code = None
    desc = 'code-description hello'
    while 1:
        j = c.pull(sub_topic, sub_client, 60, last_msgid, pull_code, desc)
        rcode = j['result-code']
        if rcode == 'NO_MORE_MESSAGE':
            break
        elif rcode != 'OK':
            print 'pull failed', rcode
            break
        msgid = j['message-id']
        content = j['content']
        print '======got', msgid, content
        
        pull_code = 'COMMIT_AND_NEXT'
        last_msgid = msgid

def produce_and_rollback(addr):
    c = IDMMClient(addr)
    c.setPassword('pub_Test')

    pub_topic = 'Test'
    pub_client = 'pub_Test'
    sub_topic = 'Test'
    sub_client = 'sub_Test'
    
    msgid = c.send(pub_topic, pub_client, u'stop 13900')
    if msgid is None:
        return False
    print 'message id=%s' %msgid
    c.send_commit(pub_topic, pub_client, msgid, 10, 13900)

    # consume
    last_msgid = None
    pull_code = None
    desc = 'code-description rollback notice'
    while 1:
        j = c.pull(sub_topic, sub_client, 60, last_msgid, pull_code, desc)
        rcode = j['result-code']
        if rcode == 'NO_MORE_MESSAGE':
            break
        elif rcode != 'OK':
            print 'pull failed', rcode
            break
        msgid = j['message-id']
        content = j['content']
        print '======got', msgid, content
        
        pull_code = 'ROLLBACK_AND_NEXT'
        last_msgid = msgid


def recv_notice(addr):
    print 'recieving notice ...'
    c = IDMMClient(addr)

    sub_topic = 'notice_1'
    sub_client = 'notice_sub_1'
    
    # consume
    last_msgid = None
    pull_code = None
    desc = 'code-description hello'
    while 1:
        j = c.pull(sub_topic, sub_client, 60, last_msgid, pull_code, desc)
        rcode = j['result-code']
        if rcode == 'NO_MORE_MESSAGE':
            break
        elif rcode != 'OK':
            print 'pull failed', rcode
            break
        msgid = j['message-id']
        content = j['content']
        print '======got', msgid, content
        #print '--', j
        
        pull_code = 'COMMIT_AND_NEXT'
        last_msgid = msgid

def main():
    #print getBrokerList()
    #print getBrokerList('172.21.0.46:4621')
    #return
    addr = '172.21.0.46:12345' 
    #addr = '127.0.0.1:64454'
    #produce_and_consume(addr)

    produce_and_rollback(addr)
    recv_notice(addr)
    
if __name__ == '__main__':
    main()
