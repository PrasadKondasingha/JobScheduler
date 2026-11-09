package edu.ufl.ads.jobscheduler.io;

import java.util.Locale;

/** Exact wording and formatting of the assignment's graded output. */
public final class OutputFormat {
  public static final String JOB_SUBMITTED = "Job %d submitted at %d ms";
  public static final String JOB_ALREADY_EXISTS = "ERROR: Job %d already exists";
  public static final String JOB_STARTED = "Job %d started at %d ms";
  public static final String NO_WAITING_JOBS = "NO_WAITING_JOBS";
  public static final String JOB_NOT_FOUND = "ERROR: Job %d not found";
  public static final String JOB_DETAILS =
      "Job %d: submitted=%d, started=%s, actual_time=%s, completed=%s, status=%s";
  public static final String JOB_CANCELLED = "Job %d cancelled";
  public static final String CANNOT_CANCEL = "ERROR: Job %d cannot be cancelled while status is %s";
  public static final String PRIORITY_UPDATED = "Job %d priority updated to %d";
  public static final String CANNOT_UPDATE_PRIORITY =
      "ERROR: Job %d priority cannot be updated while status is %s";
  public static final String SHORT_JOBS_ENABLED = "Scheduling mode changed to SHORT_JOBS";

  public static final String JOB_STATUS = "JOB STATUS";
  public static final String STATUS_HEADER =
      "Job       Submitted        Started   Actual Time   Completed      Status";
  public static final String STATUS_ROW = "%-9d %-16d %-9s %-13s %-14s %s";
  public static final String END_JOB_STATUS = "END JOB STATUS";
  public static final String UNKNOWN_TIME = "null";

  public static final String EXECUTION_ORDER = "EXECUTION_ORDER";
  public static final String EXECUTION_ROW = "%d %d %d";
  public static final String END_EXECUTION_ORDER = "END_EXECUTION_ORDER";
  public static final String TREE = "TREE";
  public static final String TREE_LEVEL = "LEVEL %d: %s";
  public static final String TREE_NODE = "[%d]";
  public static final String MISSING_CHILD = "NULL";
  public static final String TREE_SEPARATOR = "|";
  public static final String END_TREE = "END_TREE";
  public static final String INORDER = "INORDER: %s";
  public static final String MAX_HEAP = "MAX_HEAP: %s";
  public static final String HEAP_ENTRY = "(%d,%d,%d)";
  public static final String LIST_SEPARATOR = ", ";
  public static final String LIST_START = "[";
  public static final String LIST_END = "]";

  public static final String TREE_STATS = "TREE_STATS";
  public static final String RECORDS = "records=%d";
  public static final String HEIGHT = "height=%d";
  public static final String INSERTIONS = "insertions=%d";
  public static final String DELETIONS = "deletions=%d";
  public static final String LEFT_ROTATIONS = "left_rotations=%d";
  public static final String RIGHT_ROTATIONS = "right_rotations=%d";
  public static final String END_TREE_STATS = "END_TREE_STATS";
  public static final String HEAP_STATS = "HEAP_STATS";
  public static final String SIZE = "size=%d";
  public static final String MAX_SIZE = "max_size=%d";
  public static final String EXTRACTIONS = "extractions=%d";
  public static final String REMOVALS = "removals=%d";
  public static final String PRIORITY_UPDATES = "priority_updates=%d";
  public static final String REBUILDS = "rebuilds=%d";
  public static final String END_HEAP_STATS = "END_HEAP_STATS";

  private OutputFormat() {}

  /** Keep numeric output independent of the machine's default locale. */
  public static String format(String template, Object... values) {
    return String.format(Locale.ROOT, template, values);
  }
}
