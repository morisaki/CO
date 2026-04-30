/* a DECORATOR class for encryption
 */
public class SecuredChannel extends CommLayer{

	private EncryptionStream encrypt; // encryption state machine for sending
	private DecryptionStream decrypt; // decryption state machine for receiving

	public SecuredChannel(String key, CommChannel channel){

		baseChannel = channel;
		encrypt = new EncryptionStream(key);
		decrypt = new DecryptionStream(key);
	}

	public Result open(){

		return baseChannel.open();
	}

	public Result transmit(Datablock d) {

		Datablock encrypted = new Datablock(encrypt.encryptedsize(d.size()));
		encrypt.encrypt(d, encrypted);
		Result r = baseChannel.transmit(encrypted);
		return r;
	}

	public Result receive(Datablock d) {

		Datablock encrypted = new Datablock();
		Result r = baseChannel.receive(encrypted);
		decrypt.decrypt(encrypted, d);
		return r;
	}


	public Result reset(){

		if (baseChannel.state() == State.FAILED){
			// cannot reset an encrypted connection, because losing data
			// invalidates the internal state of the encryption:
			return Result.IMPOSSIBLE;
		} else {
			// nothing to do here. Do whatever the underlying channel suggests:
			return baseChannel.reset();
		}
	}

	public Result close(){
		((CommChannel) encrypt).reset(); // set back to initial state for next connection
		((CommChannel) decrypt).reset(); // set back to initial state for next connection
		return baseChannel.close();
	}

}

