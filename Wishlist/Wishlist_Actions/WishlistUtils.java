package tests.UserServices.Wishlist.Wishlist_Actions;

import org.json.simple.JSONObject;
import tests.UserServices.Common.Constant.ApiConstant;
import tests.UserServices.Common.Utils.GeneralUtilities;
import tests.UserServices.Common.Utils.JsonHelper;
import tests.UserServices.Common.Utils.ObjectMapHelpers.ResponseAddGet;
import tests.UserServices.Common.Utils.ObjectMapHelpers.ResponseByAccountID;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.flipkart.website.testng.Assertion.assertTrue;

/**
 * Created by mahesh.nayak on 31/12/14.
 */
public class WishlistUtils {

    private String[] digitalListings = {"LSTDGBDVQX4EP4KDMUYTPELOH","LSTDGBDVQX28X8YD8BHE5F2AU","LSTDGBDURSHBDJBKGXCXS8M4K","LSTDGBDURSV3HFGSMEQVOWAZN","LSTDGBDWXZ8AFXTEZAGC2DVFY"};
    private String[] physicalListings = {"LSTACCCWPAJDRFYMWAG09FQYJ","LSTKITDHNNK62HUG5ZJGN66UY","LSTBOK9780143418764YW9TLB","LSTBOK97802978709207IDNN7"};

    private JsonHelper jsH;
    private GeneralUtilities generalUtilities;
    private ApiConstant.WishlistConstants constants = null;

    public WishlistUtils(){
        jsH  = new JsonHelper();
        generalUtilities = new GeneralUtilities();
        constants = new ApiConstant.WishlistConstants();
    }

    public boolean isSorted(boolean isAscending,List<String> sortedArray) {
        if (isAscending) {

            for (int i = 0; i < sortedArray.size() - 1; i++) {
                if (sortedArray.get(i).compareTo(sortedArray.get(i + 1)) > 0)
                    return false;
            }
        }
        else{
            for (int i = 0; i < sortedArray.size() - 1; i++) {
                if (sortedArray.get(i).compareTo(sortedArray.get(i+1)) < 0)
                    return false;
            }
        }
        return true;
    }



    public Integer getCountOfProductType(String productType, ResponseByAccountID response){
        int count = 0;
        List<ResponseAddGet> list = response.getWishlistAddRs();

        for(ResponseAddGet rAg : list)
        {
            if (rAg.getProduct_type().toUpperCase().equals(productType))
                count++;
        }
        return  count;
    }

    public List<String> getSortedArrayFromJson(String response,String key,int countOfNumberOfItems){
        String[] sortedArray = new String[countOfNumberOfItems];
        int count = 0;
        List<String> sortedArray1 = new ArrayList<String>();
        ResponseByAccountID rAID = (ResponseByAccountID) jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ACCOUNT_ID);
        List<ResponseAddGet> list = rAID.getWishlistAddRs();

        assertTrue(list.size() == countOfNumberOfItems, "Wishlist size is incorrect");

        for(ResponseAddGet rAG : list)
        {
            sortedArray1.add(rAG.getCreation_date());
            count++;
        }
        return sortedArray1;
    }

    //------------------- Product listing functions------------------------------
    public  String getPhysicalItem()
    {
        Random random = new Random();
        return physicalListings[random.nextInt(physicalListings.length)];
    }

    public  String getDigitalItem()
    {
        Random random = new Random();
        return digitalListings[random.nextInt(digitalListings.length)];
    }

    public String[] getDigitalListings(){
        return digitalListings;
    }

    public String[] getPhysicalListings(){
        return physicalListings;
    }

    //-------------------Wishlist data functions ---------------------
    public String getRandomWishlistData()
    {
        return getRandomWishlistData(null);
    }

    public String getRandomWishlistData(String accId)
    {
        Random random = new Random();
        String accountId = null;

        if( accId == null ) {
            accountId = generalUtilities.getRandomAccountId();
        }
        else {
            accountId = accId;
        }

        JSONObject data = new JSONObject();
        data.put("account_id" , accountId);
        Integer flipCoin = Math.abs(random.nextInt());
        if(flipCoin %2 == 0)
            data = getPhysicalItemWishlistObject(data);
        else
            data = getDigitalItemWishlistObject(data);
        return data.toString();
    }

    public JSONObject getPhysicalItemWishlistObject(JSONObject data)
    {

        data.put("product_id", getPhysicalItem());
        data.put("product_type", "NON_DIGITAL");
        data.put("creating_system", "web");
        return data;
    }

    public JSONObject getDigitalItemWishlistObject(JSONObject data)
    {

        data.put("product_id", getDigitalItem());
        data.put("product_type", "DIGITAL");
        data.put("creating_system", "web");
        return data;
    }
}
