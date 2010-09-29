package org.obm.sync.push.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FolderHierarchy implements List<Folder> {

	private List<Folder> folders;

	public FolderHierarchy(List<Folder> folders) {
		this.folders = new ArrayList<Folder>(folders.size()+1);
		this.folders.addAll(folders);
	}

	public int size() {
		return folders.size();
	}

	public boolean isEmpty() {
		return folders.isEmpty();
	}

	public boolean contains(Object o) {
		return folders.contains(o);
	}

	public Iterator<Folder> iterator() {
		return folders.iterator();
	}

	public Object[] toArray() {
		return folders.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return folders.toArray(a);
	}

	public boolean add(Folder e) {
		return folders.add(e);
	}

	public boolean remove(Object o) {
		return folders.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return folders.containsAll(c);
	}

	public boolean addAll(Collection<? extends Folder> c) {
		return folders.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Folder> c) {
		return folders.addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return folders.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return folders.retainAll(c);
	}

	public void clear() {
		folders.clear();
	}

	public boolean equals(Object o) {
		return folders.equals(o);
	}

	public int hashCode() {
		return folders.hashCode();
	}

	public Folder get(int index) {
		return folders.get(index);
	}

	public Folder set(int index, Folder element) {
		return folders.set(index, element);
	}

	public void add(int index, Folder element) {
		folders.add(index, element);
	}

	public Folder remove(int index) {
		return folders.remove(index);
	}

	public int indexOf(Object o) {
		return folders.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return folders.lastIndexOf(o);
	}

	public ListIterator<Folder> listIterator() {
		return folders.listIterator();
	}

	public ListIterator<Folder> listIterator(int index) {
		return folders.listIterator(index);
	}

	public List<Folder> subList(int fromIndex, int toIndex) {
		return folders.subList(fromIndex, toIndex);
	}

}
