#coding=utf8

import mysql.connector
from datetime import date, datetime, timedelta

from kazoo.client import KazooClient
import logging
from kazoo.client import KazooState
import time



def zk_setversion(n):
    logging.basicConfig()
    zk = KazooClient(hosts='127.0.0.1:2181')
    def my_listener(state):
        if state == KazooState.LOST:
            print 'zk: lost'
        elif state == KazooState.SUSPENDED:
            print 'zk: suspended'
        else:
            print 'zk: connected'
            #zk.stop()
    zk.add_listener(my_listener)
    zk.start()

    zk.set('/idmm2/configServer/version', str(n))
    print 'version', zk.get('/idmm2/configServer/version')
    

    #print dir(zk)
    zk.stop()

    #time.sleep(10.0)
    print 'done'

    #zk.stop()
    

def load_conf(fname):
    tns = []
    tbls = file(fname).read().split(';\n')
    print len(tbls)
    for t in tbls:
        tns.append(t[t.find(' `')+2:t.find('` ')])
    return tbls, tns

def remove_tbls(tns, n):
    cnn = conndb()
    cur = cnn.cursor()
    for t in tns:
        if t.find('%d') < 0:
            break
        tblname = t%n
        sql = 'drop table %s'%tblname
        r = 'ok'
        try:
            cur.execute(sql)
        except:
            r = 'fail'
        print sql, r
    cur.close()
    cnn.close()

# 配置数据版本累加
def update_conf(tns, sqls, n, n1):
    cnn = conndb()
    cur = cnn.cursor()
    for i in range(len(tns)):
        sql = sqls[i].strip()
        if sql.find('%d') < 0:
            break
        tn = tns[i]%n
        tn1 = tns[i]%n1
        sql = sql%n1
        r = 'ok'
        try:
            cur.execute(sql)
            cur.execute('insert into %s select * from %s'%(tn1, tn))
        except:
            r = 'fail'
        print 'create:', tn1, r
    cur.execute('update priority_map_%d set pvalue=400 where pname=%%s'%n1, ('low',))
    cur.close()
    cnn.close()

def main1():
    sqls, tns = load_conf('conf-tables.sql')

    #if version change up 1 or 0
    up = 1
    if up != 1:
        #remove_tbls(tns, 9)
        zk_setversion(8)
    else:
        #update_conf(tns, sqls, 8, 9)
        zk_setversion(9)
    #zk_test()

##   ALTER TABLE a CONVERT TO CHARACTER SET utf8mb4;


#INSERT INTO `msgidx_part_0` (`idmm_msg_id`, `produce_cli_id`, `src_topic_id`, `dst_cli_id`, `dst_topic_id`, `src_commit_code`, `group_id`, `priority`, `idmm_resend`, `consumer_resend`, `create_time`, `broker_id`, `req_time`, `commit_code`, `commit_time`, `commit_desc`, `next_topic_id`, `next_client_id`) 
#VALUES ('1440122302471::11282::10.162.200.72:43600::283', 'Pub101', 'TRecOprCntt', 'Sub119Opr', 'TRecOprCnttDest', NULL, '226111006', 4, NULL, 0, 1440122302482, '', 0, '', 0, NULL, NULL, NULL);

def insert(cnx):
    cursor = cnx.cursor()
    sql = """INSERT INTO `msgidx_part_0` (`idmm_msg_id`, `produce_cli_id`, `src_topic_id`, `dst_cli_id`, `dst_topic_id`, `src_commit_code`, `group_id`, `priority`, `idmm_resend`, `consumer_resend`, `create_time`, `broker_id`, `req_time`, `commit_code`, `commit_time`, `commit_desc`, `next_topic_id`, `next_client_id`) 
VALUES (%s, %s, %s, %s, %s, NULL, %s, 4, NULL, 0, %s, '', %s, '', 0, NULL, NULL, NULL);
"""
    sqls = []
    for i in range(10):
        sqls.append(sql.replace('msgidx_part_0', 'msgidx_part_%d'%i))
    list=[]
    c = 0
    c1 = 0
    i = 0
    for line in file('d:/temp/11.sql'):
        strs = line.replace("'", "").replace(' ', '').split(",")
        if len(strs) != 18:
            continue
        data = (strs[0], strs[1], strs[2], strs[3], strs[4], strs[6], strs[10], int(strs[12]))
        list.append(data)

        if i > 500:
            try:
                cursor.executemany(sql,list)
                cnx.commit()
            except Exception,e:
                cnx.rollback()
                print 'ignore...', e
            c += i
            xx = c / 50000 % len(sqls)
            print '========', xx
            sql = sqls[xx]
            print("insert: %d"%c)
            i=0
            list = []
            
        i=i+1
    if i>0:
        cursor.executemany(sql,list)
        cnx.commit()
    print("ok")    

def test_insert(co):
    cur = co.cursor()

    #cur.execute('set names utf8mb4')

    '''add_employee = ("INSERT INTO employees "
               "(first_name, hire_date, birth_date) "
               "VALUES (%s, %s, %s)")
    tomorrow = datetime.now().date() + timedelta(days=1)
    data_employee = ('Geert', tomorrow, date(1977, 6, 14))
    cur.execute(add_employee, data_employee)
    emp_no = cur.lastrowid
    print 'emp_no', emp_no'''
    
    sql = "insert into a(name, grade) values(%s, %s);"
    cur.execute(sql, (u'abc 中 vvv', 102.0))
    #cur.execute("insert into a(name, grade) values('中', 11.0)")
    print '-----insert return', cur.rowcount
    co.commit()
    cur.close()

def test_select(cur):
    cur.execute("select * from a")
    while True:
        r = cur.fetchone()
        if r is None:
            break
        print r

def conndb():
    config = {
      'user': 'root',
      'password': '',
      'host': '127.0.0.1',
      'port': 3306,
      'database': 'idmm2',
      'raise_on_warnings': True,
      'charset': 'utf8',
    }
    cnx = mysql.connector.connect(**config)
    cnx.set_autocommit(True)
    return cnx

def main():
    config = {
      'user': 'root',
      'password': '',
      'host': '127.0.0.1',
      'port': 3306,
      'database': 'test',
      'raise_on_warnings': True,
      'charset': 'utf8',
    }    
    #cnx = mysql.connector.connect(user='root', password='',
                              #host='127.0.0.1',
                              #port=3306,
                              #database='idmm2_idx')
    cnx = mysql.connector.connect(**config)
    cnx.set_autocommit(True)
    #insert(cnx)
    cur = cnx.cursor()
    test_insert(cnx)
    test_select(cur)
    cur.close()
    cnx.close()

def alter_tables():
    co = conndb()
    cur = co.cursor()
    sql_str = """ALTER TABLE `msgidx_part_%d`
        CHANGE COLUMN `commit_desc` `commit_desc` VARCHAR(1024) NULL DEFAULT NULL COMMENT '消费结果描述'
            COLLATE 'utf8mb4_unicode_ci' AFTER `commit_time`"""
    for i in range(10):
        sql = sql_str % i
        cur.execute(sql)
    cur.close()
    co.close()

if __name__ == '__main__':
    alter_tables()
    
    
