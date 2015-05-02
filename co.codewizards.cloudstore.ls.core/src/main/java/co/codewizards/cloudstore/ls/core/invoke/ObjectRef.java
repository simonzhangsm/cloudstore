package co.codewizards.cloudstore.ls.core.invoke;

import static co.codewizards.cloudstore.core.util.AssertUtil.*;

import java.io.Serializable;

import co.codewizards.cloudstore.core.dto.Uid;

public class ObjectRef implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Virtual method indicating that the associated {@link ObjectRef} should be removed from the {@link ObjectManager}.
	 * <p>
	 * This method is "virtual", because it does not exist and actually has a name that is illegal as a Java method name
	 * (thus making sure, we'll never have a collision with a real method).
	 * This special name is transferred as method-name in the remote-invocation-protocol whenever a proxy on the other side
	 * is garbage-collected and the corresponding ObjectRef-to-Object-mapping should thus be removed from the
	 * {@link ObjectManager}.
	 */
	public static final String VIRTUAL_METHOD_NAME_REMOVE_OBJECT_REF = "***removeObjectRef***";

	private final Uid clientId;
	private final int classId;
	private final long objectId;

	public ObjectRef(final Uid clientId, final int classId, final long objectId) {
		this.clientId = assertNotNull("clientId", clientId);
		this.classId = classId;
		this.objectId = objectId;
	}

	public Uid getClientId() {
		return clientId;
	}
	public int getClassId() {
		return classId;
	}
	public long getObjectId() {
		return objectId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clientId.hashCode();
		result = prime * result + (int) (objectId ^ (objectId >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ObjectRef other = (ObjectRef) obj;
		return this.objectId == other.objectId && this.clientId.equals(other.clientId);
	}

	@Override
	public String toString() {
		return String.format("%s[%s, %s, %s]", this.getClass().getSimpleName(), clientId, classId, objectId);
	}
}