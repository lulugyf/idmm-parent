
import cx_Oracle

def __conndb__():
    #co = cx_Oracle.connect('idmm/ll@xe')
    co = cx_Oracle.connect('DBOFFONADM/dbaccopr200606@centdb')
    cur = co.cursor()
    return co, cur

def idx_tables(n=10):
    sql = '''CREATE TABLE %s (
  idmm_msg_id varchar(60) NOT NULL,
  produce_cli_id varchar(32) NULL,
  src_topic_id varchar(32) NULL,
  dst_cli_id varchar(32) NOT NULL,
  dst_topic_id varchar(32) NOT NULL,
  src_commit_code varchar(4) NULL,
  group_id varchar(32) NULL,
  priority number(11) NOT NULL ,
  idmm_resend number(11) NULL,
  consumer_resend number(11) NULL,
  create_time number(20) NULL,
  broker_id varchar(21) NULL,
  req_time number(20) NULL,
  commit_code varchar(4) NULL,
  commit_time number(20) NULL,
  commit_desc varchar(1024) NULL,
  next_topic_id varchar(32) NULL,
  next_client_id varchar(32) NULL,
  expire_time number(20),
  PRIMARY KEY (idmm_msg_id,dst_cli_id,dst_topic_id)
)'''
    sql_idx = 'create Index %s_idx on %s(dst_cli_id,dst_topic_id)'

    sql_store = '''CREATE TABLE messagestore_%d (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
)'''


    co, cur = __conndb__()

    for i in range(n):
        print i
        table_name = 'msgidx_part_%d'%i
        try:
            cur.execute("drop table %s"%table_name) # drop table
        except: pass
        sql1 = sql % table_name
        cur.execute(sql1)  # create table

        sql1 = sql_idx %(table_name, table_name)
        cur.execute(sql1) # create index

        table_name = "msgidx_part_his_%d" % i
        try:
            cur.execute("drop table %s" % table_name) # drop history table
        except: pass
        cur.execute(sql % table_name)  # create table of history
    for i in range(n):
        try:
            cur.execute("drop table messagestore_%d" % i)
        except: pass
        cur.execute(sql_store % i)
    cur.close()
    co.close()


def clear_tables():
    co, cur = __conndb__()
    print "clear msgidx_part_{i} ..."
    for i in range(10):
        print i
        cur.execute("truncate table msgidx_part_%d" % i)

    print "clear messagestore_{i} ..."
    for i in range(10):
        print i
        cur.execute("truncate table messagestore_%d" % i)

    cur.close()
    co.close()

    print "done"

if __name__ == '__main__':
    idx_tables()
    #clear_tables()
