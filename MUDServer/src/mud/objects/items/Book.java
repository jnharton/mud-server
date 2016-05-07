package mud.objects.items;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.interfaces.Editable;
import mud.misc.SlotTypes;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.utils.Utils;

/* genericized or not? */


public class Book extends Item implements Editable {
	private String title = "";
	private String author = "";
	private List<List<String>> pages;
	
	private Integer currentPage = 0;
	
	public Book() {
		this("", "", 0);
	}
	
	public Book(String bookTitle) {
		this(bookTitle, "", 0);
	}

	public Book(String bookTitle, String bookAuthor) {
		this(bookTitle, bookAuthor, 0);
	}

	public Book(String bookTitle, String bookAuthor, int pages) {
		super(-1);
		
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		
		this.type = TypeFlag.ITEM;
		
		this.item_type = ItemTypes.BOOK;
		
		this.title = bookTitle;
		this.author = bookAuthor;
		
		this.pages = new ArrayList<List<String>>(pages);
	}
	
	// TODO make sure this properly duplicates the template, which it doesn't atm
	protected Book( Book template ) {
		this(template.title, template.author, template.pages.size());
		
		this.pages.addAll( template.pages );
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other constructors
	 * that has parameters.
	 *
	 * 
	 * @param tempName
	 * @param tempDesc
	 * @param tempLoc
	 * @param tempDBREF
	 * @param tCharges
	 * @param spellName
	 */
	public Book(String tempName, String tempDesc, int tempLoc, int tempDBREF) {
		super(tempDBREF, tempName, EnumSet.noneOf(ObjectFlag.class), tempDesc, tempLoc);
		
		this.item_type = ItemTypes.BOOK;
		this.slot_type = SlotTypes.NONE;
		
		this.pages = new ArrayList<List<String>>(0);
	}
	
	@Override
	public String getName() {
		return getTitle();
	}
	
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
		//if( text.equals("+newpage") )
		getPage(currentPage).add(text);
	}
	
	public List<String> getPage(int pageNum) {
		return this.pages.get(pageNum);
	}

	public void setPage(final Integer newPageNum, final List<String> list) {
		if( newPageNum > -1 ) {
			if( this.pages.get(newPageNum) != null ) {
				this.pages.set(newPageNum, list);
			}
			else this.pages.add(list);
		}
	}
	
	public void addPage(final List<String> list) {
		this.pages.add(list);
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