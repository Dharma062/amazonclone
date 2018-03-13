/*
 * Created by Sandeep Tadepalli on 04/02/18 03:15
 * Copyright (c) 2018. All rights reserved.
 */

package com.ooad.web.dao;

import com.ooad.web.model.Item;
import com.ooad.web.model.Offer.DiscountOffer;
import com.ooad.web.model.Offer.Offer;
import com.ooad.web.model.Seller;
import com.ooad.web.utils.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemDao {
    public boolean createItem(final String name, final float price, final String url, final int sellerId,
                              String description, final String brand, float height, float width,int quantity) {
        try {
            Connection con = Database.getConnection();
            PreparedStatement ps = con
                    .prepareStatement("INSERT INTO Items(name,price,url,sellerId,description,brand,height,width,quantity) VALUES (?,?,?,?,?,?,?,?,?)");
            ps.setString(1, name);
            ps.setFloat(2, price);
            ps.setString(3, url);
            ps.setInt(4, sellerId);
            ps.setString(5, description);
            ps.setString(6, brand);
            ps.setFloat(7, height);
            ps.setFloat(8, width);
            ps.setInt(9,quantity);
            ps.executeUpdate();
            con.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Item getItembyId(int id) {
        try {
            Connection con = Database.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Items WHERE id=?");
            ps.setString(1, String.valueOf(id));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return itemBuilder(rs);
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Collection<Item> getLastFiveItems() {
        try {
            Connection con = Database.getConnection();
            final List<Item> items = new ArrayList<Item>();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Items ORDER BY id DESC LIMIT 5");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final Item item = itemBuilder(rs);
                items.add(item);
            }
            con.close();
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveItem(Item item){
        try {
            Connection con = Database.getConnection();
            PreparedStatement ps = con
                    .prepareStatement("UPDATE Items SET name = ? ,price = ?,url = ?," +
                            "sellerId = ? ,description = ? ,brand = ?,height = ?,width = ? "+
                            ",quantity = ? WHERE id = ?");
            ps.setString(1, item.getName());
            ps.setFloat(2, item.getPrice());
            ps.setString(3, item.getUrl());
            ps.setInt(4, item.getSeller().getId());
            ps.setString(5, item.getDescription());
            ps.setString(6, item.getBrand());
            ps.setFloat(7, item.getHeight());
            ps.setFloat(8, item.getWidth());
            ps.setInt(9,item.getQuantity());
            ps.setInt(10,item.getId());
            ps.executeUpdate();
            con.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Item itemBuilder(ResultSet rs) throws NullPointerException, SQLException {
        if (rs == null) {
            throw new NullPointerException("Result Set");
        }
        final int id = rs.getInt("id");
        final String name = rs.getString("name");
        final float price = rs.getFloat("price");
        final String url = rs.getString("url");
        final String itemDescription = rs.getString("description");
        final String brand = rs.getString("brand");
        final int sellerId = rs.getInt("sellerId");
        final int quantity = rs.getInt("quantity");
        final float height = rs.getFloat("height");
        final float width = rs.getFloat("width");
        final int offerId = rs.getInt("offerId");
        SellerDao sellerDao = new SellerDao();
        Seller seller = sellerDao.getSeller(sellerId);
        return new Item(id, name, price, url, quantity, seller, itemDescription, brand, height,
                width, getItemDetails(id),getOffer(offerId));
    }
    private Offer getOffer(int offerId){
        try {
            Connection con = Database.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Offers WHERE id = ?");
            ps.setInt(1,offerId );
            ResultSet rs = ps.executeQuery();
            con.close();
            int offerType = rs.getInt("offerType");
            if(offerType == 201){
                float discountPercentage = rs.getFloat("discountPercentage");
                Offer o= new DiscountOffer(discountPercentage);
                return o;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONArray getItemDetails(final int itemId){
        JSONArray itemDetailsArray = new JSONArray();
        if (itemId == 0) throw new NullPointerException("Item Id can't be null");
        try {
            Connection con = Database.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM ItemDetails WHERE itemId = ?");
            ps.setInt(1,itemId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                JSONObject itemDetails = new JSONObject();
                itemDetails.put("id",rs.getInt("id") );
                itemDetails.put("key",rs.getString("key") );
                itemDetails.put("value",rs.getString("value") );
                itemDetailsArray.put(itemDetails);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemDetailsArray;
    }
}
