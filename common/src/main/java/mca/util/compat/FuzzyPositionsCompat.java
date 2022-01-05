package mca.util.compat;

import java.util.Random;
import java.util.function.Predicate;
import com.google.common.annotations.VisibleForTesting;

import net.minecraft.util.math.BlockPos;

public class FuzzyPositionsCompat {
   /**
    * Creates a fuzzy offset position within the given horizontal and vertical
    * ranges.
    *
    * @since MC 1.17
    */
   public static BlockPos localFuzz(Random random, int horizontalRange, int verticalRange) {
      int i = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
      int j = random.nextInt(2 * verticalRange + 1) - verticalRange;
      int k = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
      return new BlockPos(i, j, k);
   }

   /**
    * Returns the closest position higher than the input {@code pos} that does
    * not fulfill {@code condition}, or a position with y set to {@code maxY}.
    *
    * @since MC 1.17
    */
   @VisibleForTesting
   public static BlockPos upWhile(BlockPos pos, int maxY, Predicate<BlockPos> condition) {
      if (!condition.test(pos)) {
         return pos;
      } else {
         BlockPos blockPos;
         for(blockPos = pos.up(); blockPos.getY() < maxY && condition.test(blockPos); blockPos = blockPos.up()) {
         }

         return blockPos;
      }
   }

   /**
    * @since MC 1.17
    */
   @VisibleForTesting
   public static BlockPos downWhile(BlockPos pos, int minY, Predicate<BlockPos> condition) {
      if (!condition.test(pos)) {
         return pos;
      } else {
         BlockPos blockPos;
         for(blockPos = pos.down(); blockPos.getY() > minY && condition.test(blockPos); blockPos = blockPos.down()) {
         }

         return blockPos;
      }
   }
}
