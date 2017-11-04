package sharry.common

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import fs2.{io, Stream, Task}
import scala.collection.JavaConverters._
import _root_.io.circe.Encoder, _root_.io.circe.syntax._

import sizes._

object file {

  def apply(s: String): Path =
    Paths.get(s)

  implicit class PathOps(path: Path) {
    val absolute = path.normalize.toAbsolutePath
    def name: String = path.getFileName.toString
    def parent: Path = path.getParent

    def /(child: String): Path = absolute resolve child

    def exists: Boolean = Files.exists(path)

    def toExisting: Option[Path] = if (exists) Some(absolute) else None

    def isDirectory: Boolean = Files.isDirectory(path)

    def length: Long = Files.size(path)

    def list: Iterator[Path] =
      Files.list(path).iterator.asScala

    def mkdirs(): Task[Path] = Task.delay {
      Files.createDirectories(path)
      path
    }

    def moveTo(target: Path): Task[Path] = Task.delay {
      val file = if (isDirectory) target/name else target
      Files.move(path, file)
      file
    }

    def copyTo(target: Path): Task[Path] = Task.delay {
      val file = if (isDirectory) target/name else target
      Files.copy(absolute, file)
      file
    }

    def delete: Task[Unit] = Task.delay {
      Files.delete(path)
    }

    def readAll(chunkSize: Size): Stream[Task,Byte] =
      io.file.readAll[Task](path, chunkSize.bytes)

    def write(str: String): Path =
      Files.write(path, str.getBytes("UTF-8"), StandardOpenOption.CREATE_NEW)

    def write[A](value: A)(implicit enc: Encoder[A]): Path =
      write(value.asJson.noSpaces)
  }

}
