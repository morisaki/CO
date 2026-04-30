/* a DECORATOR class for compression
 */
public class CompressorChannel extends CommLayer{

	public CompressorChannel(CommChannel channel){

		baseChannel = channel;
	}

	public Result open(){

		return baseChannel.open();
	}

	public Result transmit(Datablock d){

		Datablock compressed = new Datablock((int)(0.8 * d.size()));
		Compressor.compress(d, compressed);
		Result r = baseChannel.transmit(compressed);
		return r;
	}

	public Result receive(Datablock d){

		Datablock compressed = new Datablock();
		Result r = baseChannel.receive(compressed);
		int r2 = Compressor.decompress(compressed, d);
		if (r2 < 0){
			fail();
			return Result.FAILURE;
		} else {
			return r;
		}
	}

	public Result reset(){

		return baseChannel.reset();
	}

	public Result close(){

		return baseChannel.close();
	}

}
