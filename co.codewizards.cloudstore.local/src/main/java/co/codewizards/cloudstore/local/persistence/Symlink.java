package co.codewizards.cloudstore.local.persistence;

import static co.codewizards.cloudstore.core.util.Util.*;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="Symlink")
public class Symlink extends RepoFile {

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String target;

	public Symlink() { }

	public String getTarget() {
		return target;
	}
	public void setTarget(final String target) {
		if (! equal(this.target, target))
				this.target = target;
	}

}
