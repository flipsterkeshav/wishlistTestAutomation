package tests.UserServices.Wishlist.Wishlist_Actions;

import com.flipkart.website.testrunner.Config;
import org.json.simple.JSONObject;
import org.junit.Assert;
import tests.UserServices.Common.Constant.ApiConstant;
import tests.UserServices.Common.Utils.GeneralUtilities;
import tests.UserServices.Common.Utils.Http;
import tests.UserServices.Common.Utils.JsonHelper;
import tests.UserServices.Common.Utils.ObjectMapHelpers.InputDataMapper;
import tests.UserServices.Common.Utils.ObjectMapHelpers.ResponseAddGet;
import tests.UserServices.Common.Utils.ObjectMapHelpers.ResponseByAccountID;

import java.util.*;

import static com.flipkart.website.testng.Assertion.assertEquals;
import static com.flipkart.website.testng.Assertion.assertTrue;
import static com.flipkart.website.testng.Logger.log;

/**
 * keshav.gupta on 12Aug15
 * mahesh.nayak on 22/12/14.
 */
public class WishlistActions {

    protected Http http = null;
    private String appserver = null;
    private GeneralUtilities genUtils = null;

    private ApiConstant.WishlistConstants constants;
    private Config config;
    private JsonHelper jsH;
    private WishlistUtils wshUtils;
    private HashMap<String,String> httpHeader;
    public WishlistActions()
    {
        String API,Port,Host;
        config = new Config();
        config.loadConfigFile();
        constants = new ApiConstant.WishlistConstants();
        jsH = new JsonHelper();
        wshUtils = new WishlistUtils();
        genUtils = new GeneralUtilities();

        //String baseURL = Config.ConfigProperties.getProperty("wishlistServiceHost");
        String baseURL="w3-wishlist-svc7.nm.flipkart.com";
        //String baseURL="10.84.177.39";
        String port = Config.ConfigProperties.getProperty("wishlistServicePort");
        String apiParams = Config.ConfigProperties.getProperty("wishlistApi");

        httpHeader=new HashMap<String, String>();
        httpHeader.put("X-Request-ID","abc");

        appserver = "http://" + baseURL + ":" + port + apiParams;
        System.out.println(appserver);
        http = new Http();

    }

    //---------------Suite start functions ------------------------
    public void wishlistTestSetup(String accountId){
        String resp = apiWishlistGetByAccountId(accountId,null);
        ResponseByAccountID rAID = (ResponseByAccountID) jsH.jsonObjectMapper(resp,constants.RESPONSE_BY_ACCOUNT_ID);
        List<ResponseAddGet> list = rAID.getWishlistAddRs();

        if(list.size() == 0) return;

        for (ResponseAddGet rAG : list)
        {
            String success = apiDeleteWishList(rAG.getAccount_id(), rAG.getProduct_id());
            System.out.println(success);
        }
    }

    //--------------- Api functions ---------------------------------
    public String apiAddItemsToWishList(String data)
    {
        String url = appserver + constants.API_CREATE_WISHLIST;
        HashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("","");
        map.put("X-Request-ID", "ABCD");
        String response = http.postIt("Add Items to wishlist", url, data);
        int statusCode = http.doPost("Add Items to wishlist", url, data);
        return response;
    }

    public String apiWishlistGetByWishlistId(String wishlistId)
    {
//        String url = appserver + String.format(constants.API_GET_WISHLIST,wishlistId);
        String url = appserver + String.format(constants.API_GET_WISHLIST_BY_ACCOUNTID,wishlistId);
        System.out.println(url);
        String response = http.get("Geting wishlist " + wishlistId, url);
        System.out.println("GetWishist:::"+response);
        return response;
    }

    public String apiDeleteWishList(String AccountID,String Pid)
    {
        String url = appserver +  String.format(constants.API_DELETE_WISHLIST,AccountID,Pid);
        System.out.println(url);
        String resp=http.delete("","http://w3-wishlist-svc1.nm.flipkart.com:25005/wishlist/v1/delete/account/ACC1464594895/product/LSTDGBDURSHBDJBKGXCXS8M4K",httpHeader);
        String response =http.delete("Deleting wishlist " + Pid, url, httpHeader);
        return response;
    }

    public String deleteAccountWishlistPair(String accountId,String productId) {
        String url = appserver +  "delete/account/"+accountId.trim()+"/product/"+productId.trim();
        String response =http.delete("Deleting wishlist for account Id:"+ accountId, url,httpHeader);
        return response;
    }



    public String apiWishlistGetByAccountId(String accountId,HashMap<String,String> data)
    {
        String url = appserver +  String.format(constants.API_GET_WISHLIST_BY_ACCOUNTID, accountId);
        if(data != null){
            Iterator it = data.entrySet().iterator();
            String urlAppend = "";
            String query ="%s=%s&";

            while (it.hasNext()){
                Map.Entry pair = (Map.Entry) it.next();
                urlAppend += String.format(query,pair.getKey(),pair.getValue());
            }
            urlAppend = urlAppend.replaceAll("&$","");
            url += "?" + urlAppend;
            System.out.println(url);
        }
        String response = http.get("getting wishlist by account id " + accountId, url, httpHeader);

        return response;
    }

    public String apiWishlistGetCount(String accountId)
    {
        String url = appserver + String.format(constants.API_GET_WISHLIST_COUNT, accountId);
        System.out.println(url);
        String response = http.get("getting wishlist count by account id " + accountId, url);
        System.out.println(response);
        return response;
    }

    public String apiGetWishlistItemByProductId(String accountId, String productId)
    {
        String url = appserver + String.format(constants.API_GET_WISHLIST_BY_PRODUCTID,accountId,productId);
        System.out.println(url);
        String response = http.get("Get wishlist itme by account and product id", url);
        System.out.println(response);
        return response;
    }




    //---------------------Api helpers---------------------
    public String helper_ApiGetWishlistByAccountIdSortTest(String accountId, int countOfNumberOfItems,String sortBy, String sortType ){
        String[]  sortedArray = new String[countOfNumberOfItems];
        JSONObject sortParameters = new JSONObject();
        sortParameters.put("sortBy",sortBy);
        sortParameters.put("sortType", sortType);

        HashMap<String,String> wishListParameters = new HashMap<String, String>();

        wishListParameters.put("sort", genUtils.getURLEncodedString(sortParameters.toString()));
        String response = apiWishlistGetByAccountId(accountId, wishListParameters);

        return response;
    }



    //--------------- Api verifiers --------------------------
    public void verifyAddResponse(String response, String data)
    {
        ResponseAddGet rAG = (ResponseAddGet) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ADD_GET);
        verifyAddResponse(rAG, data);
    }

    public void verifyAddResponse(ResponseAddGet rAG, String data)
    {
        System.out.println("Verifying responses");
        if(data != null) {
            logWishListJson(rAG);
            InputDataMapper input = (InputDataMapper) jsH.jsonObjectMapper(data, constants.INPUT_DATA);
            assertTrue(rAG.getId() != null, "Wishlist Id is null");
            assertTrue(input.getAccount_id().equals(rAG.getAccount_id()), "Account Id does not match");
            assertTrue(input.getProduct_id().equals(rAG.getProduct_id()), "Product Id does not match");
            assertTrue(input.getProduct_type().equals(rAG.getProduct_type()), "Product Type does not match");
            assertTrue(rAG.getCreation_date() != null, "Creation date is null");
        }
        else {
            System.out.println("Inside *******"+rAG.getId());
            assertTrue(rAG.getId() == null, "Wishlist Ids do not match");
            assertTrue(rAG.getAccount_id() == null, "Account Ids do not match");
            assertTrue(rAG.getProduct_id() == null, "Product Ids do not match");
            assertTrue(rAG.getCreating_system() == null, "Creating Systems do not match");
            assertTrue(rAG.getProduct_type() == null, "Product type Ids do not match");
            assertTrue(rAG.getCreation_date() == null, "Creation dates do not match");
            assertTrue(rAG.getLast_modified() == null, "Last modified do not match");
        }
    }

    public void logWishListJson(ResponseAddGet wishlistJson) {
        log("JSON contents are as follows:\n");
        log("Creating System: "+wishlistJson.getCreating_system()+"\n");
        log("AccountId: "+wishlistJson.getAccount_id()+"\n");
        log("Creation date: "+wishlistJson.getCreation_date()+"\n");
        log("Error: "+wishlistJson.getError()+"\n");
        log("ID: "+ wishlistJson.getId()+"\n");
        log("Last modified: "+wishlistJson.getLast_modified()+"\n");
        log("Product ID: "+wishlistJson.getProduct_id()+"\n");
        log("Product type: " + wishlistJson.getProduct_type() + "\n");

    }


    public void verifyGetReponsesByAccount(String responseToVerify, String inputResponse)
    {
        ResponseAddGet rVerify = (ResponseAddGet) jsH.jsonObjectMapper(responseToVerify, constants.RESPONSE_BY_ADD_GET);
        ResponseAddGet rInput = (ResponseAddGet) jsH.jsonObjectMapper(inputResponse, constants.RESPONSE_BY_ADD_GET);
        verifyGetReponsesByAccount(rVerify, rInput);
    }

    public void verifyGetReponsesByAccount(ResponseAddGet responseToVerify, ResponseAddGet inputResponse)
    {
        System.out.println("Verifying responses");
        if(inputResponse != null) {
            assertEquals(responseToVerify.getId(), inputResponse.getId(), "Account Ids do not match");
            assertEquals(responseToVerify.getAccount_id(), inputResponse.getAccount_id(), "Account Ids do not match");
            assertEquals(responseToVerify.getProduct_id(), inputResponse.getProduct_id(), "Product Ids do not match");
//            assertEquals(responseToVerify.getCreating_system(), inputResponse.getCreating_system(), "Creating Systems do not match");
            assertEquals(responseToVerify.getProduct_type(), inputResponse.getProduct_type(), "Product type Ids do not match");
            //assertEquals(responseToVerify.getCreation_date(), inputResponse.getCreation_date(), "Creation dates do not match");
//            assertEquals(responseToVerify.getLast_modified(), inputResponse.getLast_modified(), "Last modified do not match");
        }
        else
        {
            assertTrue(responseToVerify.getId() == null, "Wishlist Ids do not match");
            assertTrue(responseToVerify.getAccount_id() == null, "Account Ids do not match");
            assertTrue(responseToVerify.getProduct_id() == null, "Product Ids do not match");
            assertTrue(responseToVerify.getCreating_system() == null, "Creating Systems do not match");
            assertTrue(responseToVerify.getProduct_type() == null, "Product type Ids do not match");
            assertTrue(responseToVerify.getCreation_date() == null, "Creation dates do not match");
            assertTrue(responseToVerify.getLast_modified() == null, "Last modified do not match");
        }
    }





    ///**************** Helper functions ******************************************************
    public HashMap<String,String> addMulitpleItemsToWishList(String accountId, Integer numberOfItems)
    {

        ResponseAddGet rAG = null;
        String resp = null;
        String[] listingsDigital = wshUtils.getDigitalListings();
        String[] listingsPhysical = wshUtils.getPhysicalListings();
        Integer numberOfPhysical = numberOfItems / 2;
        Integer numberOfDigital = numberOfItems - numberOfPhysical;

        HashMap<String,String> wishListResponses = new HashMap<String, String>();
        JSONObject wishData = new JSONObject();
        wishData.put("account_id" , accountId);
        wishData.put("creating_system", "web");
        try{
            wishData.put("product_type", "DIGITAL");
            for(int count=0; count < numberOfDigital; count ++)
            {
                wishData.put("product_id" , listingsDigital[count]);
                resp = apiAddItemsToWishList(wishData.toString());
                rAG = (ResponseAddGet) jsH.jsonObjectMapper(resp, constants.RESPONSE_BY_ADD_GET);
                wishListResponses.put(rAG.getId(), resp);
                Thread.sleep(constants.SLEEP_TIME);
            }

            wishData.put("product_type","NON_DIGITAL");
//            System.out.println(wishData.get("product_type"));
            for(int count=0; count < numberOfPhysical; count ++)
            {
                wishData.put("product_id", listingsPhysical[count]);
                resp = apiAddItemsToWishList(wishData.toString());
                rAG = (ResponseAddGet) jsH.jsonObjectMapper(resp, constants.RESPONSE_BY_ADD_GET);
                wishListResponses.put(rAG.getId(),resp);
                Thread.sleep(constants.SLEEP_TIME);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("Failed to add items after some time");
        }
        return wishListResponses;
    }

    ///**************** Helper functions ******************************************************
    public ArrayList<String> addMulitpleItemsToWishListNew(String accountId, Integer numberOfItems)
    {

        ResponseAddGet rAG = null;
        String resp = null;
        String[] listingsDigital = wshUtils.getDigitalListings();
        String[] listingsPhysical = wshUtils.getPhysicalListings();
        Integer numberOfPhysical = numberOfItems / 2;
        Integer numberOfDigital = numberOfItems - numberOfPhysical;
        int sleepTime=1000;

        //HashMap<String,String> wishListResponses = new HashMap<String, String>();
        ArrayList<String> wishListResponses=new ArrayList<String>();
        JSONObject wishData = new JSONObject();
        wishData.put("account_id" , accountId);
        wishData.put("creating_system", "web");
        try{
            wishData.put("product_type", "DIGITAL");
            for(int count=0; count < numberOfDigital; count ++)
            {
                wishData.put("product_id" , listingsDigital[count]);
                resp = apiAddItemsToWishList(wishData.toString());
                rAG = (ResponseAddGet) jsH.jsonObjectMapper(resp, constants.RESPONSE_BY_ADD_GET);
                //wishListResponses.put(rAG.getId(), resp);
                wishListResponses.add(resp);
                Thread.sleep(sleepTime);
            }

            wishData.put("product_type","NON_DIGITAL");
            //            System.out.println(wishData.get("product_type"));
            for(int count=0; count < numberOfPhysical; count ++)
            {
                wishData.put("product_id", listingsPhysical[count]);
                resp = apiAddItemsToWishList(wishData.toString());
                rAG = (ResponseAddGet) jsH.jsonObjectMapper(resp, constants.RESPONSE_BY_ADD_GET);
                wishListResponses.add(resp);
                Thread.sleep(sleepTime);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("Failed to add items after some time");
        }
        return wishListResponses;
    }

}
