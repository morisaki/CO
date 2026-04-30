

// operation was performed, state may change
public enum Result{

	OK,
	FAILURE, // operation failed, go to 'failed' state
	IMPOSSIBLE; // operation was rejected, state is unchanged

}
