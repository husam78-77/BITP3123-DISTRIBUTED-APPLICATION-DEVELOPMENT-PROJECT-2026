package com.smartcampus.librarybooking.repository;

import com.smartcampus.librarybooking.entity.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for {@link RoomBooking} entities.
 *
 * <p>The custom query {@link #findOverlappingBookings} is central to the
 * R5 concurrency-control design â€” it is called <b>inside</b> the
 * per-room {@code ReentrantLock} critical section in
 * {@code LibraryService.bookRoom()} to detect scheduling conflicts.</p>
 */
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {
    List<RoomBooking> findByStudentId(Long studentId);
    List<RoomBooking> findByRoomId(String roomId);
    List<RoomBooking> findByStatus(String status);

    /**
     * R5 â€” OVERLAP DETECTION QUERY (Shared Mutable State Read).
     *
     * <p>Finds every <b>CONFIRMED</b> booking for a given room whose time
     * range overlaps with the requested interval.</p>
     *
     * <h3>Mathematical Proof of the Overlap Condition</h3>
     * <pre>
     * Given two intervals:
     *   Existing booking : [E_start, E_end)
     *   Requested booking: [R_start, R_end)
     *
     * They do NOT overlap if and only if one ends before the other starts:
     *   E_end  &lt;= R_start   (existing ends before requested starts)
     *     OR
     *   R_end  &lt;= E_start   (requested ends before existing starts)
     *
     * By De Morgan's law, they DO overlap if and only if:
     *   E_end  &gt; R_start   AND   R_end  &gt; E_start
     *
     * Which is equivalent to:
     *   E_start &lt; R_end    AND   E_end  &gt; R_start
     *
     * This is exactly the WHERE clause used below.
     * </pre>
     *
     * <h3>Overlap Scenarios Covered</h3>
     * <pre>
     * Case 1 â€” Partial overlap (front):
     *   Existing:  |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   Requested:      |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   E_start &lt; R_end âœ“   E_end &gt; R_start âœ“  â†’ OVERLAP
     *
     * Case 2 â€” Partial overlap (back):
     *   Existing:       |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   Requested: |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   E_start &lt; R_end âœ“   E_end &gt; R_start âœ“  â†’ OVERLAP
     *
     * Case 3 â€” Complete containment (existing inside requested):
     *   Existing:    |â”€â”€|
     *   Requested: |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   E_start &lt; R_end âœ“   E_end &gt; R_start âœ“  â†’ OVERLAP
     *
     * Case 4 â€” Complete containment (requested inside existing):
     *   Existing:  |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   Requested:   |â”€â”€|
     *   E_start &lt; R_end âœ“   E_end &gt; R_start âœ“  â†’ OVERLAP
     *
     * Case 5 â€” Exact match:
     *   Existing:  |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   Requested: |â”€â”€â”€â”€â”€â”€â”€â”€|
     *   E_start &lt; R_end âœ“   E_end &gt; R_start âœ“  â†’ OVERLAP
     *
     * Case 6 â€” Adjacent (no overlap):
     *   Existing:  |â”€â”€â”€â”€|
     *   Requested:       |â”€â”€â”€â”€|
     *   E_start &lt; R_end âœ“   E_end &gt; R_start âœ—  â†’ NO OVERLAP âœ“
     * </pre>
     *
     * @param roomId    the room identifier to check for conflicts
     * @param startTime the requested booking start time
     * @param endTime   the requested booking end time
     * @return list of conflicting CONFIRMED bookings (empty if no overlap)
     */
    @Query("SELECT rb FROM RoomBooking rb WHERE rb.roomId = :roomId " +
           "AND rb.status = 'CONFIRMED' " +
           "AND rb.startTime < :endTime " +
           "AND rb.endTime > :startTime")
    List<RoomBooking> findOverlappingBookings(
            @Param("roomId") String roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
