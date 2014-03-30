package li.cil.oc.server.fs

import dan200.computer.api.IWritableMount
import java.io.{OutputStream, IOException}
import li.cil.oc.api.fs.Mode

class CC15WritableFileSystem(override val mount: IWritableMount)
  extends CC15FileSystem(mount)
  with OutputStreamFileSystem {

  override def delete(path: String) = try {
    mount.delete(path)
    true
  } catch {
    case _: Throwable => false
  }

  override def makeDirectory(path: String) = try {
    mount.makeDirectory(path)
    true
  } catch {
    case _: Throwable => false
  }

  override protected def openOutputHandle(id: Int, path: String, mode: Mode): Option[OutputHandle] = try {
    Some(new ComputerCraftOutputHandle(mount, mode match {
      case Mode.Append => mount.openForAppend(path)
      case Mode.Write => mount.openForWrite(path)
      case _ => throw new IllegalArgumentException()
    }, this, id, path))
  } catch {
    case _: Throwable => None
  }

  protected class ComputerCraftOutputHandle(val mount: IWritableMount, val stream: OutputStream, owner: OutputStreamFileSystem, handle: Int, path: String) extends OutputHandle(owner, handle, path) {
    override def length() = mount.getSize(path)

    override def position() = throw new IOException("bad file descriptor")

    override def write(value: Array[Byte]) = stream.write(value)
  }

}
