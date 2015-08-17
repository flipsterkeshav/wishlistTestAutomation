package tests.UserServices.Wishlist.Wishlist_Tests.src;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.testng.annotations.*;
import tests.UserServices.Common.Constant.ApiConstant;
import tests.UserServices.Common.Utils.GeneralUtilities;
import tests.UserServices.Common.Utils.JsonHelper;
import tests.UserServices.Common.Utils.ObjectMapHelpers.InputDataMapper;
import tests.UserServices.Common.Utils.ObjectMapHelpers.ResponseAddGet;
import tests.UserServices.Common.Utils.ObjectMapHelpers.ResponseByAccountID;
import tests.UserServices.Wishlist.Wishlist_Actions.WishlistActions;
import tests.UserServices.Wishlist.Wishlist_Actions.WishlistUtils;
import tests.utils.Http;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.flipkart.website.testng.Assertion.assertEquals;
import static com.flipkart.website.testng.Assertion.assertTrue;
import static com.flipkart.website.testng.Logger.log;

/**
 * Corrected and added more tests keshav.gupta on 12Aug15
 * Created by mahesh.nayak on 22/12/14.
 */

public class WishlistTest {


    private WishlistActions wishlistActions = null;
    private GeneralUtilities generalUtilities = null;
    private WishlistUtils wshUtils = null;
    private JsonHelper jsH = null;
    private String ACCOUNT_ID = null;
    private Http http = null;
    private ApiConstant.WishlistConstants constants = null;
    private HashMap<String,String> wishlistCleanupMap=new HashMap<String, String>();

    @BeforeClass(groups = {"regression", "api","smoke","test"})
    public void initialize() {
        log("Initializing");
        wishlistActions = new WishlistActions();
        generalUtilities = new GeneralUtilities();
        wshUtils = new WishlistUtils();
        jsH = new JsonHelper();
        constants = new ApiConstant.WishlistConstants();

        http = new Http();
        ACCOUNT_ID = generalUtilities.getRandomAccountId();
    }

    @AfterClass(groups = {"regression", "api","smoke","test","regression1"})
    public void cleanup() {
        wishlistActions.wishlistTestSetup(ACCOUNT_ID);
        wishlistActions = null;
    }


    public void cleanupAfterTest(HashMap<String,String> accountAndWishListIdMap) {

        Iterator it = accountAndWishListIdMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            log("Test cleanup for AccountId: "+pair.getKey()+" and productId: "+pair.getValue());
            wishlistActions.deleteAccountWishlistPair(pair.getKey().toString(),pair.getValue().toString());
        }
    }

    public void cleanUpAfterTestWrapper(ResponseByAccountID wishlistJson) {
        wishlistCleanupMap.clear();
        for(int i=0;i<wishlistJson.getWishlistAddRs().size();i++) {
            wishlistCleanupMap.put(wishlistJson.getWishlistAddRs().get(i).getAccount_id(), wishlistJson.getWishlistAddRs().get(i).getProduct_id());
            cleanupAfterTest(wishlistCleanupMap);
            wishlistCleanupMap.clear();
        }
    }

    public void cleanUpAfterTestWrapper(ResponseAddGet wishlistJson) {
        wishlistCleanupMap.clear();
            wishlistCleanupMap.put(wishlistJson.getAccount_id(), wishlistJson.getProduct_id());
            cleanupAfterTest(wishlistCleanupMap);
            wishlistCleanupMap.clear();
    }

    @BeforeMethod(groups = {"regression", "api","smoke","test","regression1"})
    public void initMethod(){
        wishlistActions.wishlistTestSetup(ACCOUNT_ID);
    }

    @AfterMethod(groups ={"regression","api","smoke","test"})
    public void sleep() throws InterruptedException {
        Thread.sleep(10);
    }

    //--------------------- Wishlist add tests------------------
    @Test(groups = {"regression", "api","smoke","regression1"},timeOut = 8000,enabled = true)
    public void createWishlist_verifyAddItemsToWishlist() {
        log("----- Executing createWishlist_verifyAddItemsToWishlist");
        log(ACCOUNT_ID);
        String data = wshUtils.getRandomWishlistData(ACCOUNT_ID);
        String response = wishlistActions.apiAddItemsToWishList(data);
        log("Create wishlist response recieved:"+response);
        log("##### Verifying Response #####");
        wishlistActions.verifyAddResponse(response, data);
        ResponseAddGet rAG = (ResponseAddGet) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ADD_GET);
        cleanUpAfterTestWrapper(rAG);
    }

    @Test(groups = {"regression", "api","smoke","regression1"},timeOut = 8000,enabled = true)
    public void createWishlist_verifyNoDuplicateWishlistCreatedForSameUserAndFSN() {
        log("----- To verify no new wishlistIsCreated for same user and product preference");
        log(ACCOUNT_ID);
        String data = wshUtils.getRandomWishlistData(ACCOUNT_ID);
        String response1 = wishlistActions.apiAddItemsToWishList(data);
        String response2=wishlistActions.apiAddItemsToWishList(data);
        log("Create wishlist response1 recieved:"+response1+"\n");
        log("Create wishlist response2 recieved:"+response1+"\n");
        log("##### Verifying Response #####");
        ResponseAddGet responseJson1 = (ResponseAddGet) jsH.jsonObjectMapper(response1,constants.RESPONSE_BY_ADD_GET);
        ResponseAddGet responseJson2 = (ResponseAddGet) jsH.jsonObjectMapper(response2,constants.RESPONSE_BY_ADD_GET);
        assertEquals(responseJson1.getId(), responseJson2.getId(), "Wishlist ID returned for same user and product are not same");
        cleanUpAfterTestWrapper(responseJson1);
        cleanUpAfterTestWrapper(responseJson2);
    }



    @Test(groups = {"regression", "api","smoke"},timeOut = 5000,enabled = true)
    public void createWishlist_verifyInvalidPayloads() {

        log("\n\n----- Executing verifyInvalidPayloads -----");
        log("\nVerify correct error messages are returned for missing payloads\n");
        ResponseAddGet responseJson = null;
        log(ACCOUNT_ID);
        String data = wshUtils.getRandomWishlistData(ACCOUNT_ID);

        JsonParser jsonParser = new JsonParser();
        JsonObject obj = (JsonObject) jsonParser.parse(data);

        obj.addProperty("account_id", "");
        String response = wishlistActions.apiAddItemsToWishList(obj.toString());
        log("\nError message for missing error ID: "+response);

        responseJson = (ResponseAddGet) jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ADD_GET);
        assertEquals(responseJson.getError(), constants.accountIdMissingError, "Test Fail for empty account id");

        obj = (JsonObject) jsonParser.parse(data);
        obj.addProperty("product_id", "");
        response = wishlistActions.apiAddItemsToWishList(obj.toString());
        log("Error message for missing product ID: "+response);
        responseJson = (ResponseAddGet) jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ADD_GET);
        assertEquals(responseJson.getError(), constants.productIdMissingError, "Test Fail for empty product id");

        obj = (JsonObject) jsonParser.parse(data);
        obj.addProperty("product_type", "");
        response = wishlistActions.apiAddItemsToWishList(obj.toString());
        log("Error message for missing product type: " + response);
        responseJson = (ResponseAddGet) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ADD_GET);
        assertEquals(responseJson.getError(), constants.productTypeIdMissingError, "Test Fail for empty Product Type");
    }


    @Test(groups = {"regression", "api","smoke"}, timeOut = 8000,enabled = true)
    public void createWishlist_verifyExistingWishlist() {

        log("Make sure same wishlist ID is returned if trying to create a new one for the same user.");
        String data  = wshUtils.getRandomWishlistData(ACCOUNT_ID);
        String wishlistResponse = wishlistActions.apiAddItemsToWishList(data);
        ResponseAddGet responseJson = (ResponseAddGet) jsH.jsonObjectMapper(wishlistResponse,constants.RESPONSE_BY_ADD_GET);
        String wishlistId = responseJson.getId();
        String response = wishlistActions.apiWishlistGetByWishlistId(ACCOUNT_ID);
        ResponseByAccountID wishlistJson = (ResponseByAccountID)jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ACCOUNT_ID);

        assertEquals(wishlistJson.getWishlistAddRs().get(0).getId(), wishlistId, "Wishlist id do not match");
        cleanUpAfterTestWrapper(responseJson);
    }

    @Test(groups = {"regression", "api","smoke","test"}, timeOut = 5000,enabled = true)
    public void getWishList_verifyGetNonExistingWishlist() {
        log("\n\n----- Executing verifyGetNonExistingWishlist -----");
        String wishlistId = "WSNONEXISTING1234";
        String response = wishlistActions.apiWishlistGetByWishlistId(wishlistId);
        ResponseAddGet w = (ResponseAddGet)jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ADD_GET);
        wishlistActions.verifyAddResponse(w, null);
    }

    @Test(groups = {"regression", "api","smoke"},timeOut = 5000,enabled = true)
    public void createWishlist_verifyMultipleAddAndGetOfWishlistItems() throws JSONException, InterruptedException {

        log("\n\n Will create a wishlist with 4 products and verify if returned by the GET call");
        Integer countOfNumberOfItems = 4; //Max of 5
        HashMap<String,String>  wishListResponses = wishlistActions.addMulitpleItemsToWishList(ACCOUNT_ID, countOfNumberOfItems);
        String response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID, null);
        ResponseByAccountID wishlistJsonObject = (ResponseByAccountID)jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ACCOUNT_ID);
        log("##### Verifying Response #####");
        assertTrue(wishlistJsonObject.getCount().equals(countOfNumberOfItems.toString()), "Count is incorrect");
        assertTrue(wishlistJsonObject.getFiltered_count().equals(countOfNumberOfItems.toString()), "Filtered count is incorrect");

        cleanUpAfterTestWrapper(wishlistJsonObject);
    }

    @Test(groups = {"regression","api","smoke"},timeOut = 20000,enabled = true)
    public void getWishlist_verifySortingOfWishlistItems(){

        log("\n\n----- Executing verifySortingOfWishlistItems -----");
        wishlistActions.wishlistTestSetup(ACCOUNT_ID);
        Integer countOfNumberOfItems = 9; //Max of 9
        List<String> sortedArray = new ArrayList<String>();
        String response = null;
        wishlistActions.addMulitpleItemsToWishList(ACCOUNT_ID, countOfNumberOfItems);

        //Verify ascending
        response = wishlistActions.helper_ApiGetWishlistByAccountIdSortTest(ACCOUNT_ID, countOfNumberOfItems,"creation_date","asc");
        ResponseByAccountID wishlistJsonObject = (ResponseByAccountID)jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ACCOUNT_ID);
        sortedArray = wshUtils.getSortedArrayFromJson(response, "wishlist", countOfNumberOfItems);

        log("##### Verifying Response #####");
        assertTrue(wshUtils.isSorted(true, sortedArray), "Wishlist is not sorted in ascending order");

        cleanUpAfterTestWrapper(wishlistJsonObject);
    }

    @Test(groups = {"regression","api","smoke"},timeOut = 20000,enabled = true)
    public void getWishlist_verifyWishlistByAccountIdForStepAndCounts(){

        log("\nWill verify if the step and count filters are working fine for get wishlist API\n");
        log("\n\n----- Executing verifyGetWishlistByAccountIdWithParametersNegativeCases -----");
        ResponseByAccountID rAID = null;
        HashMap<String,String> wishlistGetParameters = new HashMap<String, String>();
        int countOfNumberOfItems = 4; //Max of 9
        int step = 0;

        HashMap<String, String> wishListResponses = wishlistActions.addMulitpleItemsToWishList(ACCOUNT_ID,countOfNumberOfItems);

        //Step = 0 case
        log("\nVerifying for step equal to zero\n");
        wishlistGetParameters.put("count",String.valueOf(step));
        wishlistGetParameters.put("start",String.valueOf(0));
        String response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID, wishlistGetParameters);
        rAID = (ResponseByAccountID) jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ACCOUNT_ID);

        assertTrue(rAID.getWishlistAddRs().size() == 0, "Step size 0 test is incorrect");

        log("\nStep size more than the number of items\n");
        step = countOfNumberOfItems + 100;
        wishlistGetParameters.put("count",String.valueOf(step));
        wishlistGetParameters.put("start", String.valueOf(0));
        response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID, wishlistGetParameters);
        rAID = (ResponseByAccountID) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ACCOUNT_ID);

        assertTrue(rAID.getWishlistAddRs().size() == countOfNumberOfItems, "Step size > no. of items test is incorrect");

        log("\nVerify incorrect start position\n");
        wishlistGetParameters.put("start", String.valueOf(20));
        response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID, wishlistGetParameters);
        rAID = (ResponseByAccountID) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ACCOUNT_ID);

        assertTrue(rAID.getWishlistAddRs().size() == 0,"Step size is not equal to 0 as expected");

        response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID, null);
        ResponseByAccountID wishlistJsonObject = (ResponseByAccountID)jsH.jsonObjectMapper(response, constants.RESPONSE_BY_ACCOUNT_ID);

        cleanUpAfterTestWrapper(wishlistJsonObject);
    }

    @Test(groups = {"regression","api","smoke"},enabled = true)
    public void getWishlist_verifyGetWishlistPaginationByAccountId(){

        log("\n\n----- Executing verifyGetWishlistByAccountIdWithParameters -----");
        log(ACCOUNT_ID);
        log("\nMake sure pagination works for wishlist. Here a wishlist of 4 products " +
                "is created and results are verifed across spread of 2 pages.");
        Integer countOfNumberOfItems = 4; //Max of 9
        Integer step = 2;

        //HashMap<String,String> wishListResponses = wishlistActions.addMulitpleItemsToWishList(ACCOUNT_ID, countOfNumberOfItems);
        ArrayList<String> wishListResponses=wishlistActions.addMulitpleItemsToWishListNew(ACCOUNT_ID, countOfNumberOfItems);
        ArrayList<String> wishListResponsesReversed= new ArrayList<String>();
        for(int i=wishListResponses.size()-1;i>-1;i--) {
            wishListResponsesReversed.add(wishListResponses.get(i));
        }


        HashMap<String,String> wishlistGetParameters = new HashMap<String, String>();

        wishlistGetParameters.put("count",String.valueOf(step));
        for (int i = 0 ; i<countOfNumberOfItems ;i = i + step) {
            wishlistGetParameters.put("start",String.valueOf(i));
            String response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID, wishlistGetParameters);

            ResponseByAccountID rAID = (ResponseByAccountID)jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ACCOUNT_ID);
            List<ResponseAddGet> listOfWishlist = rAID.getWishlistAddRs();
            //for(ResponseAddGet rAg : listOfWishlist)
            for(int j=0;j<listOfWishlist.size();j++)
            {
                ResponseAddGet rAg=listOfWishlist.get(j);
                //String inputDataForCurrentId = wishListResponses.get(((i+2)%countOfNumberOfItems)+j);
                String inputDataForCurrentId = wishListResponsesReversed.get(i+j);
                ResponseAddGet rIn = (ResponseAddGet)jsH.jsonObjectMapper(inputDataForCurrentId,constants.RESPONSE_BY_ADD_GET);
                if(!rIn.getProduct_id().equals(rAg.getProduct_id())) {
                    if((i+j)%2==0) {
                        //if even
                        inputDataForCurrentId = wishListResponsesReversed.get(i+j+1);
                        rIn = (ResponseAddGet)jsH.jsonObjectMapper(inputDataForCurrentId,constants.RESPONSE_BY_ADD_GET);
                    }
                    else {
                        inputDataForCurrentId = wishListResponsesReversed.get(i+j-1);
                        rIn = (ResponseAddGet)jsH.jsonObjectMapper(inputDataForCurrentId,constants.RESPONSE_BY_ADD_GET);
                    }
                }
                System.out.println("##### Verifying Response #####");
                wishlistActions.verifyGetReponsesByAccount(rAg, rIn);
            }
            System.out.println(response);
            cleanUpAfterTestWrapper(rAID);
        }
    }


    @Test(groups = {"regression","api","smoke"},timeOut = 20000,enabled = true)
    public void verifyGetWishlistApiFiltering()
    {
        log("\n\n----- Executing verifyGetWishlistApiFiltering -----\n");
        log("Make sure for a wishlist, able to apply filter for product type\n");
        ResponseByAccountID rAID = null;
        log(ACCOUNT_ID);
        Integer countOfNumberOfItems = 4; //Max of 9
        HashMap<String,String> wishListResponses = wishlistActions.addMulitpleItemsToWishList(ACCOUNT_ID, countOfNumberOfItems);
        String response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID,null);
        rAID = (ResponseByAccountID) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ACCOUNT_ID);

        Integer countDigital = wshUtils.getCountOfProductType("DIGITAL", rAID);
        Integer countPhysical = wshUtils.getCountOfProductType("NON_DIGITAL",rAID);

        JSONObject filterParameters = new JSONObject();
        HashMap<String,String> wishlistParameters = new HashMap<String, String>();

        //Filter by product type
        filterParameters.put("productType","DIGITAL");
        wishlistParameters.put("filter", generalUtilities.getURLEncodedString(filterParameters.toString()));
        response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID,wishlistParameters);
        rAID = (ResponseByAccountID) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ACCOUNT_ID);

        Integer obtainedCount = rAID.getWishlistAddRs().size();

        log("##### Verifying Response ##### for product type digital");
        assertTrue(Integer.parseInt(rAID.getFiltered_count()) == countDigital, "Filtered count is not correct");
        assertTrue(obtainedCount == countDigital, "ProductType digital count is not correct");

        cleanUpAfterTestWrapper(rAID);
        //Filter non existInG productType
        filterParameters.put("productType","NON_EXISITNG");
        wishlistParameters.put("filter", generalUtilities.getURLEncodedString(filterParameters.toString()));
        response = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID,wishlistParameters);
        rAID = (ResponseByAccountID) jsH.jsonObjectMapper(response,constants.RESPONSE_BY_ACCOUNT_ID);

        obtainedCount = rAID.getWishlistAddRs().size();

        log("##### Verifying Response #####");
        assertTrue(Integer.parseInt(rAID.getFiltered_count()) == 0, "Filtered count is not correct");
        assertTrue(obtainedCount == 0, "ProductType digital count is not correct");
        cleanUpAfterTestWrapper(rAID);
    }

     //---------------------Wishlist getCount Api test--------------------------------
     @Test(groups = {"regression", "api","smoke"},timeOut = 5000,enabled = true)
     public void createWishlist_verifyGetCountApi(){

         log("\n\n----- Executing verifyGetCountApi -----");
         log(ACCOUNT_ID);
         String Pid ="PID";
//         wishlistActions.apiDeleteWishList(ACCOUNT_ID);
         String response = wishlistActions.apiWishlistGetCount(ACCOUNT_ID);
         if (Integer.parseInt(response)!=0)
             Assert.fail("Count of 0 not obtained");
         Integer countOfNumberOfItems = 4; //Max of 9
         wishlistActions.addMulitpleItemsToWishList(ACCOUNT_ID, countOfNumberOfItems);
         response =  wishlistActions.apiWishlistGetCount(ACCOUNT_ID);
         System.out.println("##### Verifying Response #####");
         assertTrue(Integer.parseInt(response) == countOfNumberOfItems,"Count does not match");
     }

    //--------------------- Wishlist delete tests------------------
    @Test(groups = {"regression", "api","smoke"},timeOut = 5000,enabled = true)
    public void verifyDeleteExistingWishlist() {

        log("\n\n----- Executing verifyDeleteExistingWishlist -----");
        log(ACCOUNT_ID);
        String data  = wshUtils.getRandomWishlistData(ACCOUNT_ID);
        String wishlistResponse = wishlistActions.apiAddItemsToWishList(data);
        log("wishlistResponse::" + wishlistResponse);

        ResponseAddGet rAID = (ResponseAddGet) jsH.jsonObjectMapper(data,constants.RESPONSE_BY_ADD_GET);
        String wishlistId =  rAID.getId();
        String Pid="Pid";
        String delResponse = wishlistActions.apiDeleteWishList(rAID.getAccount_id(),rAID.getProduct_id());

        log("##### Verifying Response #####");
        assertTrue(Boolean.parseBoolean(delResponse) == true, "Delete not successful");
        wishlistResponse = wishlistActions.apiWishlistGetByAccountId(ACCOUNT_ID,null);

        ResponseAddGet w = (ResponseAddGet)jsH.jsonObjectMapper(wishlistResponse, constants.RESPONSE_BY_ADD_GET);

        log("##### Verifying Response #####");
        wishlistActions.verifyGetReponsesByAccount(w, null);

    }

    @Test(groups = {"regression", "api","smoke"},timeOut = 5000,enabled = true)
    public void verifyDeleteNonExistingWishlist() {

        System.out.println("\n\n----- Executing verifyDeleteNonExistingWishlist -----");
        String wishlistId = "WSNONEXISTING1234";
        String productId ="PID";
        String ACCID ="ACCID";

        String response = wishlistActions.apiDeleteWishList(productId,ACCID);

        log("##### Verifying Response #####");
        assertTrue(Boolean.parseBoolean(response) == true,constants.accountIdMissingError);
    }

    //---------------Get wishlist item by product id------------------
    //@Test(groups = {"regression","api","smoke"},timeOut = 5000,enabled = true)
    public void verifyGetWishlistItemByProductId() throws InterruptedException {
        log("\n\n----- Executing verifyGetWishlistItemByProductId -----");
        log(ACCOUNT_ID);
        String data = wshUtils.getRandomWishlistData(ACCOUNT_ID);

        InputDataMapper iDM = (InputDataMapper) jsH.jsonObjectMapper(data, constants.INPUT_DATA);
        String productId = iDM.getProduct_id();

        String wishListResponse = wishlistActions.apiAddItemsToWishList(data);
        System.out.println("wishListResponse"+wishListResponse);
        Thread.sleep(10000);
        String wishlistItemByProductIdResponse = wishlistActions.apiGetWishlistItemByProductId(ACCOUNT_ID, productId);
        System.out.println("wishlistItemByProductIdResponse"+wishlistItemByProductIdResponse);

        System.out.println("##### Verifying Response #####");
        wishlistActions.verifyGetReponsesByAccount(wishListResponse, wishlistItemByProductIdResponse);
    }
}