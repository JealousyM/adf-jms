package by.perevertkin.view.beans;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.output.RichOutputText;

import javax.jms.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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

    public JmsAdfBean() throws NamingException, JMSException {
        initialContext = new InitialContext();
        qconFactory = (QueueConnectionFactory) initialContext.lookup(JMS_FACTORY);
        queue = (Queue) initialContext.lookup(QUEUE);
    }

    public void close() throws JMSException {
        qsession.close();
        qcon.close();
    }

    public void sendMessage(ActionEvent actionEvent) throws JMSException, NamingException {
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        qsender = qsession.createSender(queue);
        msg = qsession.createTextMessage();
        qcon.start();
        msg.setText(getSendMessageText().getValue().toString());
        qsender.send(msg);
        qsender.close();
        close();
    }

    public void readMessage(ActionEvent actionEvent) throws NamingException, JMSException {
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        qreceiever = qsession.createReceiver(queue);
        msg = qsession.createTextMessage();
        qcon.start();
        TextMessage textMessage = (TextMessage) qreceiever.receive();
        recieveMessageText.setValue(textMessage.getText());
        qreceiever.close();
        close();
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
}
