package io.github.kastork

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.AsyncFile
import io.vertx.core.file.OpenOptions
import io.vertx.core.impl.Utils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

class VertxFileSystemSpec extends Specification
{
  Vertx vertx;
  AsyncConditions conditions

  @Shared
  String DEFAULT_DIR_PERMS = "rwxr-xr-x"

  @Shared
  String DEFAULT_FILE_PERMS = "rw-r--r--"

  @Shared
  String pathSep

  @Shared
  String testDir

  @Rule
  TemporaryFolder temporaryFolder

  def setup()
  {
    vertx = Vertx.vertx()
    java.nio.file.FileSystem fs = FileSystems.getDefault();
    pathSep = fs.getSeparator();
    File ftestDir = temporaryFolder.newFolder();
    testDir = ftestDir.toString();
  }

  def cleanup()
  {
    vertx.close()
  }

  def "Test simple async copy"()
  {
    setup:

    conditions = new AsyncConditions(1)
    String source = "foo.txt";
    String target = "bar.txt";
    createFileWithJunk(source, 100);

    when:

    vertx.fileSystem().copy("$testDir$pathSep$source", "$testDir$pathSep$target") {
      conditions.evaluate {
        assert new File(testDir, source).exists()
        assert new File(testDir, target).exists()
      }
    }

    then:

    conditions.await();
  }

  def "Test IllegalArguments"()
  {
    when: "Copying from null"
    vertx.fileSystem().copy(null, "ignored") {h -> 1}

    then: "Should throw"
    thrown(NullPointerException)

    when:
    vertx.fileSystem().writeFileBlocking(null, Buffer.buffer())

    then:
    thrown(NullPointerException)

    when:

    String fileName = "some-file.dat";
    AsyncFile asyncFile = vertx
        .fileSystem()
        .openBlocking(testDir + pathSep + fileName, new OpenOptions())
    asyncFile.write(Buffer.buffer(), -1) {h -> 1}

    then:
    thrown(IllegalArgumentException)
  }

  // TODO: Refactor in the style of groovy/spock
  // For many of these util functions, I just copied
  // them to get things going quickly

  // copy of java method in the Vertx sources
  private void createFile(String fileName, byte[] bytes) throws Exception
  {
    File file = new File(testDir, fileName);
    Path path = Paths.get(file.getCanonicalPath());
    Files.write(path, bytes);

    setPerms(path, DEFAULT_FILE_PERMS);
  }

  // copy of java method in the Vertx sources
  private void createFileWithJunk(String fileName, long length) throws Exception
  {
    createFile(fileName, randomByteArray((int) length));
  }

  // copy of java method in the Vertx sources
  private void setPerms(Path path, String perms)
  {
    if (Utils.isWindows() == false)
    {
      try
      {
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(perms));
      }
      catch (IOException e)
      {
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  // copy of java method in the Vertx TestUtils sources
  public static byte[] randomByteArray(int length)
  {
    return randomByteArray(length, false, (byte) 0);
  }

  // essentially a copy of java method in the Vertx TestUtils sources
  public static byte[] randomByteArray(int length, boolean avoid, byte avoidByte)
  {
    byte[] line = new byte[length];
    for (int i = 0; i < length; i++)
    {
      byte rand;
      if (avoid)
      {
        while (true)
        {
          rand = randomByte();
          if (rand != avoidByte) break;
        }
      }
      line[i] = rand;
    }
    return line;
  }

  public static byte randomByte()
  {
    return (byte) ((int) (Math.random() * 255) - 128);
  }
}
