package mud.objects.items;

import java.util.ArrayList;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;

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
		this.flags = EnumSet.noneOf(ObjectFlag.class);
				
		this.type = TypeFlag.ITEM;
		this.item_type = ItemType.BOOK;
		this.title = "";
		this.author = "";
		this.pages = new ArrayList<ArrayList<String>>();
	}
	
	public Book(String bookTitle) {
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		
		this.type = TypeFlag.ITEM;
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
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		
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
		super(tempDBREF, tempName, EnumSet.noneOf(ObjectFlag.class), tempDesc, tempLoc);
		
		this.type = TypeFlag.ITEM;
		this.item_type = ItemType.BOOK;
		this.slot_type = SlotType.NONE;
		
		this.pages = new ArrayList<ArrayList<String>>(0);
	}

	@Override
	public String read() {
		return this.getPage(currentPage).toString();
	}

	@Override
	public void write(String text) {
		getPage(currentPage).add(text);
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
		output[0] = this.getDBRef() + "";              // database reference number
		output[1] = this.getName();                    // name
		output[2] = TypeFlag.asLetter(this.type) + ""; // flags
		output[2] = output[2] + getFlagsAsString();
		output[3] = this.getDesc();                    // description
		output[4] = this.getLocation() + "";           // location
		output[5] = this.item_type.ordinal() + "";     // item type
		output[6] = this.getAuthor();                  // book author
		output[7] = this.getTitle();                   // book title
		output[8] = this.pages.size() + "";            // # of pages
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}