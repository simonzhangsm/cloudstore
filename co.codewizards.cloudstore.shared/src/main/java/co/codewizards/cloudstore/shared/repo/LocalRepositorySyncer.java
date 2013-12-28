package co.codewizards.cloudstore.shared.repo;

import static co.codewizards.cloudstore.shared.util.Util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.codewizards.cloudstore.shared.persistence.Directory;
import co.codewizards.cloudstore.shared.persistence.NormalFile;
import co.codewizards.cloudstore.shared.persistence.RepoFile;
import co.codewizards.cloudstore.shared.persistence.RepoFileDAO;
import co.codewizards.cloudstore.shared.progress.ProgressMonitor;
import co.codewizards.cloudstore.shared.util.HashUtil;

class LocalRepositorySyncer {

	private static final Logger logger = LoggerFactory.getLogger(LocalRepositorySyncer.class);

	private final RepositoryTransaction transaction;
	private final RepoFileDAO repoFileDAO;
	private final File localRoot;

	public LocalRepositorySyncer(RepositoryTransaction transaction) {
		this.transaction = assertNotNull("transaction", transaction);
		repoFileDAO = new RepoFileDAO().persistenceManager(this.transaction.getPersistenceManager());
		localRoot = this.transaction.getRepositoryManager().getLocalRoot();
	}

	public void sync(ProgressMonitor monitor) { // TODO use this monitor!!!
		sync(null, localRoot);
	}

	private void sync(RepoFile parentRepoFile, File file) {
		RepoFile repoFile = repoFileDAO.getRepoFile(localRoot, file);

		// If the type changed - e.g. from normal file to directory - we must delete
		// the old instance.
		if (repoFile != null && !isRepoFileTypeCorrect(repoFile, file)) {
			deleteRepoFile(repoFile);
			repoFile = null;
		}

		if (repoFile == null) {
			repoFile = createRepoFile(parentRepoFile, file);
			if (repoFile == null) { // ignoring non-normal files.
				return;
			}
		} else if (isModified(repoFile, file)) {
			updateRepoFile(repoFile, file);
		}

		Set<String> childNames = new HashSet<String>();
		File[] children = file.listFiles(new FilenameFilterSkipMetaDir());
		if (children != null) {
			for (File child : children) {
				childNames.add(child.getName());
				sync(repoFile, child);
			}
		}

		Collection<RepoFile> childRepoFiles = repoFileDAO.getChildRepoFiles(repoFile);
		for (RepoFile childRepoFile : childRepoFiles) {
			if (!childNames.contains(childRepoFile.getName())) {
				deleteRepoFile(childRepoFile);
			}
		}
	}

	private boolean isRepoFileTypeCorrect(RepoFile repoFile, File file) {
		// TODO support symlinks!
		if (file.isFile())
			return repoFile instanceof NormalFile;

		if (file.isDirectory())
			return repoFile instanceof Directory;

		return true;
	}

	private boolean isModified(RepoFile repoFile, File file) {
		if (file.isFile()) {
			if (!(repoFile instanceof NormalFile))
				throw new IllegalArgumentException("repoFile is not an instance of NormalFile!");

			NormalFile normalFile = (NormalFile) repoFile;

			long difference = Math.abs(normalFile.getLastModified().getTime() - file.lastModified());
			// TODO make time difference threshold configurable
			if (difference > 5000)
				return true;

			return normalFile.getLength() != file.length();
		} else {
			return false;
		}
	}

	private RepoFile createRepoFile(RepoFile parentRepoFile, File file) {
		if (parentRepoFile == null)
			throw new IllegalStateException("Creating the root this way is not possible! Why is it not existing, yet?!???");

		// TODO support symlinks!

		RepoFile repoFile;

		if (file.isDirectory()) {
			repoFile = new Directory();
		} else if (file.isFile()) {
			NormalFile normalFile = (NormalFile) (repoFile = new NormalFile());
			normalFile.setLastModified(new Date(file.lastModified()));
			normalFile.setLength(file.length());
			normalFile.setSha1(sha(file));
		} else {
			logger.warn("File is neither a directory nor a normal file! Skipping: {}", file);
			return null;
		}

		repoFile.setParent(parentRepoFile);
		repoFile.setName(file.getName());

		// For consistency reasons, we touch the parent not only when deleting a child (which is required
		// by our sync algorithm), but also when adding a new child.
		parentRepoFile.setChanged(new Date());

		return repoFileDAO.makePersistent(repoFile);
	}

	private void updateRepoFile(RepoFile repoFile, File file) {
		if (file.isFile()) {
			if (!(repoFile instanceof NormalFile))
				throw new IllegalArgumentException("repoFile is not an instance of NormalFile!");

			NormalFile normalFile = (NormalFile) repoFile;
			normalFile.setLastModified(new Date(file.lastModified()));
			normalFile.setLength(file.length());
			normalFile.setSha1(sha(file));
		}
	}

	private void deleteRepoFile(RepoFile repoFile) {
		RepoFile parentRepoFile = repoFile.getParent();
		if (parentRepoFile == null)
			throw new IllegalStateException("Deleting the root is not possible!");

		// We must ensure the parent's localRevision + changed properties are updated. Though this happens
		// automatically whenever the object is changed, we need to touch it somehow. We use setChanged(...)
		// to do this, even though exactly this property is set automatically (because of the
		// AutoTrackChanged interface).
		parentRepoFile.setChanged(new Date());

		repoFileDAO.deletePersistent(repoFile);
	}

	private String sha(File file) {
		assertNotNull("file", file);
		if (!file.isFile()) {
			return null;
		}
		try {
			FileInputStream in = new FileInputStream(file);
			byte[] hash = HashUtil.hash(HashUtil.HASH_ALGORITHM_SHA, in);
			in.close();
			return HashUtil.encodeHexStr(hash, 0, hash.length);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
