package com.wfh.transaction;

public class TransactionHolder {
    //ThreadLocal为容器,能存放一个值,只能当前线程获取,其他线程无法获取值
    private static ThreadLocal<Transaction> transactionHolder = new ThreadLocal<>();

    //获取事务的方法
    public static Transaction getTransaction() {
        Transaction transaction = transactionHolder.get();
        if (transaction == null) {//事务不存在就创建一个存入transactionHolder
            transaction = new Transaction();
            transactionHolder.set(transaction);
        }
        return transaction;
    }

    public static void setTransaction(Transaction transaction) {
        transactionHolder.set(transaction);
    }

    public static void removeTransaction(){
        //移除事务释放内存
        if (transactionHolder.get()!=null){
            transactionHolder.remove();
        }
    }

}
