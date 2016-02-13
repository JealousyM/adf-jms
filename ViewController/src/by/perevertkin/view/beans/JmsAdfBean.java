package by.perevertkin.view.beans;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.output.RichOutputText;

import javax.jms.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import oracle.adf.share.logging.ADFLogger;

public class JmsAdfBean {
    private RichOutputText recieveMessageText;
    private RichInputText sendMessageText;

    public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    public final static String JMS_FACTORY = "jms/ADFConnectionFactory";
    public final static String QUEUE = "jms/ADFJMSQueue";

    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueSender qsender;
    private QueueReceiver qreceiever;
    private Queue queue;
    private TextMessage msg;
    private Context initialContext;
    private RichInputText repeatCount;
    
    private static ADFLogger logger = 
                ADFLogger.createADFLogger(JmsAdfBean.class); 

    /*  
     * initial JMS
     * */    

    public JmsAdfBean() {
        try{
        initialContext = new InitialContext();
        qconFactory = (QueueConnectionFactory) initialContext.lookup(JMS_FACTORY);
        queue = (Queue) initialContext.lookup(QUEUE);

        } catch (NamingException e) {
            logger.severe("NamingException in constructor():"+e.getMessage());
        }
    }


    /*
     * Close session and connection
     * */
    
    public void close() throws JMSException {
        qsession.close();
        qcon.close();
    }
    
    /*
     * Send message to WebLogic JMS
     * */    
    
    public void sendMessage(ActionEvent actionEvent) {
        try {
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        qsender = qsession.createSender(queue);
        msg = qsession.createTextMessage();
        qcon.start();
        Integer count = Integer.valueOf(getRepeatCount().getValue().toString());
        for (int i = 0; i < count; i++) {
            msg.setText(getSendMessageText().getValue().toString());
            qsender.send(msg);
        }
        qsender.close();
        close();

        } catch (JMSException e) {
            logger.severe("JMSException in sendMessage():"+e.getMessage());
        }
    }

    /*
     * Read message from WebLogic JMS
     * */    
    public void readMessage(ActionEvent actionEvent) {
        try {
            String returnJms = "Msg:";
            qcon = qconFactory.createQueueConnection();
            qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            qreceiever = qsession.createReceiver(queue);
            msg = qsession.createTextMessage();
            qcon.start();
            Message m;
            do {
                m = qreceiever.receive(1);
                if (m != null) {
                    if (m instanceof TextMessage) {
                        msg = (TextMessage) m;
                        returnJms = returnJms + msg.getText() + " ";
                    } else {
                        break;
                    }
                }
            } while (m != null);

            recieveMessageText.setValue(returnJms);
            qreceiever.close();
            close();

        } catch (JMSException e) {
            logger.severe("JMSException in readMessage():"+e.getMessage());
        }

    }

    public void setSendMessageText(RichInputText sendMessageText) {
        this.sendMessageText = sendMessageText;
    }

    public RichInputText getSendMessageText() {
        return sendMessageText;
    }

    public void setRecieveMessageText(RichOutputText recieveMessageText) {
        this.recieveMessageText = recieveMessageText;
    }

    public RichOutputText getRecieveMessageText() {
        return recieveMessageText;
    }

    public void setRepeatCount(RichInputText repeatCount) {
        this.repeatCount = repeatCount;
    }

    public RichInputText getRepeatCount() {
        return repeatCount;
    }
}
