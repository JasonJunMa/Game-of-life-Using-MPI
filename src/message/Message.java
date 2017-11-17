package message;

public class Message {
	private int signal;

    /**
     * Constructor.
     * @param value the signal type.
     */
    public Message(int value)
    {
        this.signal = value;
    }

    /**
     * Getter.
     * @return the message type.
     */
    public int getSignal()
    {
        return signal;
    }
}
