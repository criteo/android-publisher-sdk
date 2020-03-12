package com.criteo.publisher.bid;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.Clock;
import java.util.UUID;

public class UniqueIdGenerator {

  @NonNull
  private final Clock clock;

  public UniqueIdGenerator(@NonNull Clock clock) {
    this.clock = clock;
  }

  /**
   * Generate a new unique ID suitable for Criteo backends
   * <p>
   * The output represents a 32 bytes unique ID formatted into hexadecimal. The 8 first (MSB) bytes
   * represent the UNIX timestamp in seconds. The 24 following ones are random from a cryptographic
   * random generator. This ensures keeping a very low probability of collision.
   * <p>
   * At SDK level, a simpler {@link UUID#randomUUID()} would be sufficient. But those ids are
   * expected to be sent to a Criteo backend and should then be (almost) unique at Criteo level and
   * scale with it.
   * <p>
   * This is based on an algorithm already done in C# and Scala to generate impression id.
   * <p>
   * Generation IDs are suitable for:
   * <ul>
   *   <li>Impression ID</li>
   * </ul>
   *
   * @see <a href="https://review.crto.in/gitweb?p=publisher/direct-bidder.git;a=blob;f=directbidder-app/src/main/scala/com/criteo/directbidder/helpers/ImpressionIdHelper.scala;hb=master">
   *   CDB ImpressionIdHelper</a>
   * @see <a href="https://review.crto.in/gitweb?p=adserving-backend/criteo-arbitration.git;a=blob;f=Criteo.Arbitration.Protocol/ArbitrageId.cs;hb=master">
   *   Arbitrage ArtbitrageId</a>
   *
   * @return a new ID
   */
  @NonNull
  public String generateId() {
    long timeInSecond = clock.getCurrentTimeInMillis() / 1000;
    return generateId(UUID.randomUUID(), timeInSecond);
  }

  @NonNull
  @VisibleForTesting
  String generateId(@NonNull UUID uuid, long timeInSecond) {
    long msb = uuid.getMostSignificantBits();
    long lsb = uuid.getLeastSignificantBits();

    // Move 1st byte at 13th position. And 2nd one at 17th (which is the 1st of LSB)
    // 13th and 17th digits are not random in UUID spec, so we put random ones instead.
    msb = setByteAt(msb, 12, getByteAt(msb, 0));
    lsb = setByteAt(lsb, 0, getByteAt(msb, 1));

    // Paste in the timestamp at the 8 MSB
    msb = (timeInSecond << 32) | (msb & 0xFFFFFFFFL);

    return String.format("%016x%016x", msb, lsb);
  }

  /**
   * Return the byte in the given value at the given index.
   * <p>
   * The index is from left to right, so the 1st one represent the MSB byte of the value and the
   * 15th represent the LSB byte.
   *
   * @param value     value to read the byte from
   * @param byteIndex index from left to right of the byte to read
   * @return byte at given index
   */
  private byte getByteAt(long value, int byteIndex) {
    int index = 64 - (byteIndex + 1) << 2;
    return (byte) ((value & 0xFL << index) >> index & 0xF);
  }

  /**
   * Set the given byte in the given value at given index.
   * <p>
   * The index is from left to right, so the 1st one represent the MSB byte of the value and the
   * 15th represent the LSB byte.
   *
   * @param value     value to set byte in
   * @param byteIndex index (from left to right) where to set the byte
   * @param byteToSet byte to inject at given index
   * @return value with the byte set
   */
  private long setByteAt(long value, int byteIndex, byte byteToSet) {
    int index = 64 - (byteIndex + 1) << 2;
    long valueWithoutDestination = value & ~(0xFL << index);
    long byteToCopyAtDestination = ((long) byteToSet) << index;
    return valueWithoutDestination | byteToCopyAtDestination;
  }

}
