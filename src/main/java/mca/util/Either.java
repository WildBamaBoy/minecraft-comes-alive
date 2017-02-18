package mca.util;

public class Either<L, R> 
{
	private final L left;
	private final R right;
	
	private Either(L left, R right)
	{
		this.left = left;
		this.right = right;
	}
	
	public static <L, R> Either<L, R> withL(L l)
	{
		return new Either<L, R>(l, null);
	}

	public static <L, R> Either<L, R> withR(R r)
	{
		return new Either<L, R>(null, r);
	}
	
	public L getLeft()
	{
		return left;
	}
	
	public R getRight()
	{
		return right;
	}
}
