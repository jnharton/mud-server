package mud.objects.items;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mud.ObjectFlag;
import mud.interfaces.Editable;
import mud.misc.SlotTypes;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.utils.Utils;

public class Book extends Item implements Editable {
	private String title = "";
	private String author = "";
	
	private Integer currentPage = 0;
	
	private Map<Integer, List<String>> pages;
	
	public Book() {
		this("", "", 0);
	}
	
	public Book(final String bTitle) {
		this(bTitle, "", 0);
	}

	public Book(final String bTitle, final String bAuthor) {
		this(bTitle, bAuthor, 0);
	}

	public Book(final String bTitle, final String bAuthor, final int pages) {
		super(-1, "book", "a book");
		
		this.item_type = ItemTypes.BOOK;
		
		this.title = bTitle;
		this.author = bAuthor;
		
		this.pages = new HashMap<Integer, List<String>>();
	}
	
	// TODO make sure this properly duplicates the template, which it doesn't atm
	protected Book(final Book template) {
		this(template.title, template.author, template.pages.size());
		
		this.pages.putAll( template.pages );
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other constructors
	 * that has parameters.
	 *
	 * 
	 * @param name
	 * @param description
	 * @param location
	 * @param dbref
	 * @param tCharges
	 * @param spellName
	 */
	public Book(final int dbref, final String name, final EnumSet<ObjectFlag> flags, final String description, final int location) {
		super(dbref, name, EnumSet.noneOf(ObjectFlag.class), description, location);
		
		this.item_type = ItemTypes.BOOK;
		this.slot_type = SlotTypes.NONE;
		
		this.pages = new HashMap<Integer, List<String>>();
	}
	
	/*@Override
	public String getName() {
		return getTitle();
	}*/
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(final String newTitle) {
		this.title = newTitle;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public void setAuthor(final String newAuthor) {
		this.author = newAuthor;
	}
	
	public Integer getPageNum() {
		return this.currentPage;
	}
	
	public void setPageNum(int newPageNum) {
		this.currentPage = newPageNum;
	}
	
	/* Editable Methods */
	@Override
	public String read() {
		return this.getPage(currentPage).toString();
	}

	@Override
	public void write(final String text) {
		if( text.equals("+newpage") ) {
			addPage( Utils.mkList("") );
		}
		else {
			getPage(currentPage).add(text);
		}
	}
	
	/**
	 * getPage(int pageNum)
	 * 
	 * Gets the contents of the specified page or returns an empty list.
	 * 
	 * @param pageNum
	 * @return
	 */
	public List<String> getPage(int pageNum) {
		if( this.pages.containsKey(pageNum) ) {
			return this.pages.get(pageNum);
		}
		else {
			return Collections.emptyList();
		}
		
	}

	public void setPage(final Integer newPageNum, final List<String> list) {
		if( newPageNum > -1 ) {
			this.pages.put(newPageNum, list);
		}
	}
	
	public void addPage(final List<String> list) {
		this.pages.put(this.pages.size() + 1, list);
	}
	
	/* Navigation */
	
	public void turnToPage(int newPage) {
		if( newPage > -1 ) {
			if( newPage < numPages() ) this.currentPage = newPage;
			else                       this.currentPage = numPages() - 1;
		}
		else this.currentPage = 0;
	}
	
	public List<String> nextPage() {
		this.currentPage++;
		return getPage(this.currentPage);
	}
	
	public List<String> prevPage() {
		this.currentPage--;
		return getPage(this.currentPage);
	}
	
	public int numPages() {
		return this.pages.size();
	}
	
	@Override
	public Book getCopy() {
		return new Book(this);
	}

	@Override
	public String toDB() {
		final String[] output = new String[3];
		
		output[0] = this.getAuthor();       // book author
		output[1] = this.getTitle();        // book title
		output[2] = this.pages.size() + ""; // # of pages
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}