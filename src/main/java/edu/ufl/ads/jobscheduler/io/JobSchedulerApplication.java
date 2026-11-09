package edu.ufl.ads.jobscheduler.io;

import edu.ufl.ads.jobscheduler.scheduler.JobScheduler;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JobSchedulerApplication {
  private JobSchedulerApplication() {}

  public static void main(String[] args) throws IOException {
    Objects.requireNonNull(args, "Command-line arguments must not be null");
    if (args.length != 1) {
      System.err.println("Usage: java jobScheduler <input_file>");
      return;
    }
    run(Path.of(args[0]));
  }

  private static void run(Path inputPath) throws IOException {
    List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
    List<String> output = new ArrayList<>();
    int capacity = readCapacity(lines);
    JobScheduler scheduler = new JobScheduler(capacity);
    boolean capacityRead = false;
    for (String line : lines) {
      if (line.isBlank()) {
        continue;
      }
      if (!capacityRead) {
        capacityRead = true;
        continue;
      }
      output.addAll(dispatch(line.trim().split("\\s+"), scheduler));
    }
    output.addAll(scheduler.finalStructureOutput());
    writeOutput(outputPath(inputPath), output);
  }

  private static int readCapacity(List<String> lines) {
    for (String line : lines) {
      if (line.isBlank()) {
        continue;
      }
      String[] tokens = line.trim().split("\\s+");
      if (tokens.length != 2 || !tokens[0].equals("CAPACITY")) {
        throw new IllegalArgumentException("Expected CAPACITY <k> as the first non-empty line");
      }
      return Integer.parseInt(tokens[1]);
    }
    throw new IllegalArgumentException("Input file is empty");
  }

  private static List<String> dispatch(String[] tokens, JobScheduler scheduler) {
    validateCommand(tokens);
    long timestamp = Long.parseLong(tokens[0]);
    scheduler.completeThrough(timestamp);
    String command = tokens[1];
    return switch (command) {
      case "SUBMIT" ->
          scheduler.submit(
              timestamp,
              Integer.parseInt(tokens[2]),
              Integer.parseInt(tokens[3]),
              Long.parseLong(tokens[4]),
              Long.parseLong(tokens[5]));
      case "PROCESS_NEXT" -> scheduler.processNext();
      case "FIND" -> scheduler.find(Integer.parseInt(tokens[2]));
      case "JOB_STATUS" -> scheduler.jobStatus();
      case "CANCEL" -> scheduler.cancel(Integer.parseInt(tokens[2]));
      case "UPDATE_PRIORITY" ->
          scheduler.updatePriority(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
      case "SHORT_JOBS" -> scheduler.switchToShortJobs();
      default -> throw new IllegalArgumentException("Unknown command: " + command);
    };
  }

  private static void validateCommand(String[] tokens) {
    if (tokens.length < 2) {
      throw new IllegalArgumentException(
          "Expected <timestamp> <command>: " + String.join(" ", tokens));
    }
    int expected =
        switch (tokens[1]) {
          case "SUBMIT" -> 6;
          case "FIND", "CANCEL" -> 3;
          case "UPDATE_PRIORITY" -> 4;
          case "PROCESS_NEXT", "JOB_STATUS", "SHORT_JOBS" -> 2;
          default -> throw new IllegalArgumentException("Unknown command: " + tokens[1]);
        };
    if (tokens.length != expected) {
      throw new IllegalArgumentException(
          tokens[1] + " requires " + (expected - 2) + " argument(s)");
    }
  }

  private static Path outputPath(Path inputPath) {
    Path fileName = inputPath.getFileName();
    if (fileName == null) {
      throw new IllegalArgumentException("Input path must name a file: " + inputPath);
    }
    String filename = fileName.toString();
    int extension = filename.lastIndexOf('.');
    String baseName = extension > 0 ? filename.substring(0, extension) : filename;
    return inputPath.resolveSibling(baseName + "_output_file.txt");
  }

  private static void writeOutput(Path outputPath, List<String> output) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
      for (String line : output) {
        writer.write(line);
        writer.newLine();
      }
    }
  }
}
