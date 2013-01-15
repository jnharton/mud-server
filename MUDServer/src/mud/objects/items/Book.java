package mud.objects.items;

import java.util.ArrayList;

import mud.SlotType;
import mud.interfaces.Editable;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.utils.Utils;

/* genericized or not? */


public class Book extends Item implements Editable {

	private String title;
	private String author;
	private ArrayList<ArrayList<String>> pages;
	private Integer currentPage = 0;

	public Book() {
		this.item_type = ItemType.BOOK;
		this.title = "";
		this.author = "";
		this.pages = new ArrayList<ArrayList<String>>();
	}
	
	public Book(String bookTitle) {
		this.item_type = ItemType.BOOK;
		this.title = bookTitle;
		this.author = "";
		this.pages = new ArrayList<ArrayList<String>>();
	}

	public Book(String bookTitle, String bookAuthor) {
		this();
		this.title = bookTitle;
		this.author = bookAuthor;
	}

	public Book(String bookTitle, String bookAuthor, int pages) {
		this.item_type = ItemType.BOOK;
		this.title = bookTitle;
		this.author = bookAuthor;
		this.pages = new ArrayList<ArrayList<String>>(pages);
	}
	
	public Book( Book template ) {
		this(template.title, template.author, template.pages.size());
		this.item_type = template.item_type;
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
		super(tempDBREF, tempName, "I", tempDesc, tempLoc);
		
		this.item_type = ItemType.BOOK;
		this.st = SlotType.NONE;
		
		this.pages = new ArrayList<ArrayList<String>>(0);
	}

	@Override
	public String read() {
		return this.getPage(currentPage).toString();
	}

	@Override
	public void write(String text) {
	}
	
	public ArrayList<String> getPage(int pageNum) {
		return this.pages.get(pageNum);
	}

	public ArrayList<String> setPage(Integer newPageNum, ArrayList<String> newPage) {
		return this.pages.set(newPageNum, newPage);
	}
	
	public Integer getPageNum() {
		return this.currentPage;
	}

	public void setPageNum(Integer newPageNum) {
		this.currentPage = newPageNum;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public void setAuthor(String newAuthor) {
		this.author = newAuthor;
	}

	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String newTitle) {
		this.title = newTitle;
	}

	/* should increase the size of the array, but needs to preserve
	 * existing content and it's location in relation to the whole
	 */
	public void setSize(int newSize) {
		this.pages = new ArrayList<ArrayList<String>>(newSize);
	}

	public int getSize() {
		return this.pages.size();
	}
	
	public void turnToPage(int newPage) {
		this.currentPage = newPage;
	}
	
	public ArrayList<String> nextPage() {
		this.currentPage++;
		return getPage(this.currentPage);
	}
	
	public ArrayList<String> prevPage() {
		this.currentPage--;
		return getPage(this.currentPage);
	}
	
	public ArrayList<String> look() {
		return null;
	}
	
	@Override
	public String getName() {
		return getTitle();
	}

	@Override
	public String toDB() {
		String[] output = new String[9];
		output[0] = Utils.str(this.getDBRef());          // book database reference number
		output[1] = this.getName();                      // book name
		output[2] = this.getFlags();                     // book flags
		output[3] = this.getDesc();                      // book description
		output[4] = Utils.str(this.getLocation());       // book location
		output[5] = Utils.str(this.item_type.ordinal()); // item type
		output[6] = this.getAuthor();                    // book author
		output[7] = this.getTitle();                     // book title
		output[8] = Utils.str(this.pages.size());        // # of pages
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}